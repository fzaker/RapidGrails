package rapidgrails

import fi.joensuu.joyds1.calendar.JalaliCalendar
import grails.converters.JSON
import grails.orm.HibernateCriteriaBuilder
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.web.json.JSONArray
import org.grails.datastore.mapping.query.api.QueryableCriteria
import org.hibernate.collection.PersistentMap
import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Subqueries
import rapidgrails.reporting.ReportDataReader
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

class RapidGrailsController {
    def exportService

    def addCriteriaMethods() {
        HibernateCriteriaBuilder.metaClass.static.getHibernateDetachedCriteria = {
            QueryableCriteria queryableCriteria, String alias = null ->

                DetachedCriteria detachedCriteria
                if (alias) {
                    detachedCriteria = DetachedCriteria.forClass(
                            queryableCriteria.getPersistentEntity().getJavaClass(), alias);
                } else {
                    detachedCriteria = DetachedCriteria.forClass(
                            queryableCriteria.getPersistentEntity().getJavaClass());
                }
                populateHibernateDetachedCriteria(detachedCriteria, queryableCriteria);
                return detachedCriteria;
        }

        HibernateCriteriaBuilder.metaClass.exists = {
            grails.gorm.DetachedCriteria gormDetachedCriteria, String joinAlias ->

                addToCriteria(Subqueries.exists(
                        HibernateCriteriaBuilder.getHibernateDetachedCriteria(gormDetachedCriteria, joinAlias)
                ))
        }
        HibernateCriteriaBuilder.metaClass.notExists = {
            grails.gorm.DetachedCriteria gormDetachedCriteria, String joinAlias ->

                addToCriteria(Subqueries.notExists(
                        HibernateCriteriaBuilder.getHibernateDetachedCriteria(gormDetachedCriteria, joinAlias)
                ))
        }
    }

    def jsonList = {
        addCriteriaMethods()
        def export = params.export

        def max = Math.min(params.rows ? params.int('rows') : 10, 100)
        def page = export ? 0 : params.int("page")
        def offset = export ? 0 : (page - 1) * params.int("rows")

        def sort = params.sidx ?: null
        def order = params.sord ?: null

        def tree = params.tree
        def list = !export
        def level
        def parentId
        if (tree) {
            if (params.n_level)
                level = Integer.parseInt(params.n_level) + 1
            else
                level = 0
            parentId = params.nodeid
        }

        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(params.domainClass)
        def allAliases = [:]

        def hasDeleted = domainClass.hasPersistentProperty("deleted")
        params.max = max
        params.offset = offset
        params.sort = sort
        params.order = order


        def instanceList
        def records
        def userData

        if (params.source) {
            def source = JSON.parse(params.source)
            def serviceResponse = ReportDataReader.fromService(source.service, source.method, source.params)
            instanceList = serviceResponse.list
            userData = serviceResponse.userdata
            records = instanceList.size()
        } else {
            def query = {
                if (tree) {
                    if (!parentId)
                        isNull("${tree}")
                    else
                        eq("${tree}.id", Long.parseLong(parentId))
                }
                if (hasDeleted) {
                    or {
                        eq("deleted", false)
                        isNull('deleted')
                    }

                }
                if (params.filter) {
                    def filter = JSON.parse(params.filter)


                    def findingCriteria = { _filter, DefaultGrailsDomainClass _domainClass, aliases ->

                        def closure = {


                            _filter.each { f ->

                                if (f.op == 'exists') {
                                    def cdclass = grailsApplication.getDomainClass(f.field)
                                    Closure<?> innerClosure = getFindingCriteria(f.data, cdclass, aliases)
                                    exists(new grails.gorm.DetachedCriteria(cdclass.clazz).build {
                                        projections {
                                            property 'id'
                                        }
                                        eqProperty "${f.val}.${f.thirdParam}", 'this.id'
//                                        eq('valueStr', '1')
//                                        eq('type.id', 2L)
                                        innerClosure.setResolveStrategy(Closure.DELEGATE_ONLY)
                                        innerClosure.setDelegate(delegate)
                                        innerClosure.call()
                                    }, f.val)

                                } else if (!f.field && f.op && f.data) {
                                    "${f.op}" {
                                        Closure<?> innerClosure = getFindingCriteria(f.data, _domainClass, aliases)
                                        innerClosure.setResolveStrategy(Closure.DELEGATE_ONLY)
                                        innerClosure.setDelegate(delegate)
                                        innerClosure.call()
                                    }

                                } else {

                                    def aliasFieldParts = f.field.split(/\./)
                                    def beforeDot = aliasFieldParts[0]
                                    def v
                                    def assignVal = { val, property ->
                                        if(f.op in ['eqProperty','ltProperty','gtProperty'])
                                            v=val
                                        else{
                                            try {
                                                def type = property.type
                                                if (type.toString().equals("boolean"))
                                                    v = val as Boolean
                                                else if (type.toString().equals("int"))
                                                    v = val as Integer
                                                else if (type == Number.class)
                                                    v = val as Integer
                                                else if (type == byte[].class)
                                                    v = (val as String).bytes
                                                else if (type == String.class)
                                                    v = val as String
                                                else if (type == Long)
                                                    if (val instanceof JSONArray)
                                                        v = val.collect { it.toLong() }
                                                    else {
                                                        v = val as Long
                                                    }
                                                else
                                                    v = property.type.newInstance(val)
                                            } catch (e) {
                                                e.printStackTrace()
                                                v = val
                                            }
                                        }
                                    }
                                    def getProperty = { tmpDomainClass, assocProperty ->
                                        def res
                                        if (tmpDomainClass.hasProperty(assocProperty))
                                            res = tmpDomainClass.getPropertyByName(assocProperty)
                                        else {
                                            tmpDomainClass.subClasses.each {
                                                if (it.hasProperty(assocProperty))
                                                    res = it.getPropertyByName(assocProperty)
                                            }
                                        }
                                        return res
                                    }
                                    def assignSingle = { val ->
                                        if (f.op == "createAlias") {
                                            aliases.put(f.val, f.field)
                                            v = val

                                        } else if (aliases.keySet().contains(beforeDot)) {
                                            def stack = new Stack<String>()
                                            def tmpBD = beforeDot
                                            stack.push(aliasFieldParts[1])
                                            while (tmpBD && aliases.containsKey(tmpBD)) {
                                                if (aliases.get(tmpBD).contains('.')) {
                                                    def aliasFieldParts_ = aliases.get(tmpBD).split(/\./)
                                                    stack.push(aliasFieldParts_[1])
                                                    tmpBD = aliasFieldParts_[0]
                                                } else {
                                                    stack.push(aliases.get(tmpBD))
                                                    tmpBD = ''
                                                }
                                            }
                                            def tmpDomainClass = _domainClass
                                            while (stack.size() > 1) {
                                                def assocProperty = stack.pop()
                                                def assocClass = getProperty(tmpDomainClass, assocProperty).referencedPropertyType
                                                tmpDomainClass = grailsApplication.getDomainClass(assocClass.name)
                                            }
                                            def aliasFieldProperty = getProperty(tmpDomainClass, stack.pop())
                                            v = assignVal(val, aliasFieldProperty)
                                        } else if (_domainClass.hasPersistentProperty(f.field)) { // The simple case, f.field is direct field of the class
                                            def property = _domainClass.getPropertyByName(f.field)
                                            assignVal(val, property)

//                                        v = f.val.asType(property.type)
                                        } else {
                                            if (f?.field?.endsWith("id"))
                                                val as Long
                                            else
                                                val as String
                                        }
                                    }
                                    if (f.op == 'in') {
                                        v = f.val.collect { assignSingle(it) }
                                    } else if (f.op == 'eqProperty') {
                                        v = f.val
                                    } else
                                        v = assignSingle(f.val)
                                    if (f.thirdParam)
                                        "${f.op}"(f.field, v, f.thirdParam)
                                    else if (f.val)
                                        "${f.op}"(f.field, v)
                                    else
                                        "${f.op}"(f.field)
                                }
                            }
                        }
                        return closure
                    }
                    HibernateCriteriaBuilder.metaClass.getFindingCriteria = findingCriteria
                    try {
                        org.grails.datastore.gorm.mongo.MongoCriteriaBuilder.metaClass.getFindingCriteria = findingCriteria
                    } catch (x) {

                    }
                    Closure<?> c = delegate.getFindingCriteria(filter, domainClass, allAliases)
                    c.setResolveStrategy(Closure.DELEGATE_ONLY)
                    c.setDelegate(delegate)
                    c.call()
                }
                if (list) {
                    if (params.sort) {
                        String sidx = params.sort.trim()
                        if (sidx.endsWith(","))
                            sidx = sidx[0..-2]
                        else
                            sidx = sidx + " " + params.order
                        def sorts = sidx.split(",").collect {
                            def parts = it.split(" ")
                            parts = (parts - "") - " "
                            [name: parts[0].trim(), order: parts[1].trim()]
                        }
                        "${"and"}" {
                            sorts.each { sortItem ->
                                "${"order"}"(sortItem.name, sortItem.order ?: "asc")
                            }
                        }
                    }

                    if (params.offset)
                        firstResult params.offset
                    if (params.max)
                        maxResults params.max
                }
            }
//            def countQuery = {
//                if (params.filter) {
//                    def filter = JSON.parse(params.filter)
////                    filter.each {
////                        GrailsDomainClassProperty property = domainClass.getPropertyByName(it.p)
////                        def v = property.type.newInstance(it.v)
////                        "${it.o}"(it.p, v)
////                    }
//                }
//            }

            instanceList = domainClass.clazz.createCriteria().list(query).unique()
            list = false
            records = domainClass.clazz.createCriteria().count(query)
        }

        def total = (int) (records / max) + 1
        def maxColumnCount = Integer.parseInt(params.maxColumns)
        def excludedProperties = ["id", "version", "password"]
        def colNames = []
        def expressions = [:]

        if (params.columns) {
            def columns = JSON.parse(params.columns)
            columns.each {
                colNames << it.name
                if (it.expression)
                    expressions[it.name] = it.expression
            }
        } else {
            domainClass.constraints.keySet().each { pname ->
                if (!excludedProperties.contains(pname) && (colNames.size() < maxColumnCount)) {
                    colNames << pname
                }
            }
            def hasTransients = domainClass.clazz.metaClass.hasProperty(domainClass.clazz, "transients")
            if (hasTransients)
                domainClass.clazz.transients?.each { pname ->
                    if (!excludedProperties.contains(pname) && (colNames.size() < maxColumnCount))
                        colNames << pname
                }
        }

        def binding = new Binding()
        def gs = new GroovyShell(binding)
        def autoId = 0
        def rows = instanceList.collect {
            def cell = export ? [:] : []

            if (!params.showFirstColumn || Boolean.parseBoolean(params.showFirstColumn)) {
                if (export)
                    cell.id = it.id.toString()
                else
                    cell << it.id.toString()
            }
            colNames.each { col ->
                if (expressions[col]) {
                    binding.setVariable("obj", it)
                    binding.setVariable("g", g)
                    binding.setVariable("rg", rg)
                    def v
                    try {
                        v = gs.evaluate("${expressions[col]}")
                    } catch (x) { x.printStackTrace() }
                    if ((v instanceof Double) || (v instanceof Float))
                        v = String.format("%.2f", v)
                    if (export)
                        cell[col] = v
                    else
                        cell << v
                } else {
                    def v = it[col]
                    if (v instanceof Date) {
//                        if (domainClass.constraints[col]?.metaConstraints?.persian) {
                        def cal = Calendar.getInstance()
                        cal.setTime(v)
                        def jc = new JalaliCalendar(cal)
                        v = String.format("%04d/%02d/%02d", jc.getYear(), jc.getMonth(), jc.getDay())
//                        } else {
//
//                        }
                    } else if (v instanceof String || v instanceof Boolean) {
                        def code1 = "${domainClass.propertyName}.${col}.${v}"
                        def code2 = "${col}.${v}"
                        v = message(code: code1, default: message(code: code2, default: v as String))
                    } else if ((v instanceof Double) || (v instanceof Float)) {
                        v = String.format("%.2f", v)
                    } else if (v == null)
                        v = ""
                    if (export)
                        cell[col] = v?.toString()
                    else
                        cell << v?.toString()
                }
            }
            if (tree) {
                cell << level // level

                if (!parentId)
                    cell << parentId
                else
                    cell << Long.parseLong(parentId)

                def currentObject = it
                def childCountQuery = {
                    eq("${tree}", currentObject)
                    projections {
                        rowCount()
                    }
                }
                def childCount = domainClass.clazz.createCriteria().get(childCountQuery)
                cell << (childCount == 0) //isLeaf

                cell << false //expanded
            }
            [id: it.id ?: autoId++, cell: cell]
        }
        if (export) {
            def colLabels = colNames.collectEntries { def res = [:]; res[it] = message(code: "${domainClass.propertyName}.${it}"); return res }
            exportService.export("Excel", response, "export", "xls", rows.collect { it.cell }, colNames, colLabels, [:], [:])
        } else
            render([page: page.toString(), total: total, records: records.toString(),
                    rows: rows, userdata: userData] as JSON)
    }

    def jsonInstance = {
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(params.domainClass)
        def obj = domainClass.clazz.findById(params.id)
        if (obj)
            domainClass = grailsApplication.getDomainClass(obj.class.name)
        def res = [domainClassType: domainClass.fullName]
        def ignored = []
        if (domainClass.hasProperty("ignoredFieldsInJSON"))
            ignored = domainClass.clazz.ignoredFieldsInJSON
        ignored << 'springSecurityService'
        domainClass.properties.each {
            if (!ignored.contains(it.name)) {
                def val = obj[it.name]
                if (val) {
                    if (it.oneToMany || it.manyToMany) {
                        res[it.name] = []
                        val.each { item ->
                            def itemVal = [:]
                            if (val instanceof PersistentMap) {
                                itemVal[item.key] = item.value
                            } else {
                                item?.domainClass?.properties?.each {
                                    if (item[it.name])
                                        itemVal[it.name] = item[it.name]
                                }
                            }
                            res[it.name] << itemVal
                        }
                    } else
                        res[it.name] = val
                }
            }
        }
        render res as JSON
    }


    def getSampleCriteria() {
        def closure = {
            eq("firstName", "farshid")
        }
        closure
    }

    def search = {
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(params.domainClass)
        def term = Boolean.parseBoolean(params.like) ? "*${params.term}*" : params.term
        def results = domainClass.clazz.search(term).results
        def map = results.collect {
            [id: it.id, label: it.toString(), value: it.toString()]
        }
        render map as JSON
    }

    def save = {
        DefaultGrailsDomainClass domainClass
        if (params.domainClassType)
            domainClass = grailsApplication.getDomainClass(params.domainClassType)
        else
            domainClass = grailsApplication.getDomainClass(params.domainClass)
        def instance
        if (params.id)
            instance = domainClass.clazz.findById(params.id)
        else
            instance = domainClass.newInstance()

        def newParams = params.clone()
        if (instance.hasProperty("composites")) {
            def composites = instance.composites
            params.findAll { p -> composites.any { p.key.startsWith(it) } }.each {
                newParams.remove(it.key)
            }
        }
        bindData(instance, newParams)
        if (instance.hasProperty("composites")) {
            def composites = instance.composites
            composites.each { composit ->
                params.findAll { it.key.startsWith(composit) && it.value instanceof Map }
                        .each {
                    def methodName = composit[0].toUpperCase() + composit.substring(1);
                    def compositParams = it.value
                    def compositInstance
                    if (compositParams.id) {
                        compositInstance = instance."${composit}".find { (compositParams.id as Long) == it.id }
                    } else {
                        compositInstance = domainClass.propertyMap[composit].referencedDomainClass.clazz.newInstance()

                        instance."addTo${methodName}"(compositInstance)
                    }
                    bindData(compositInstance, compositParams)
                    compositInstance.deleted = compositParams?.deleted?.toBoolean()
                }
            }
        }
        bindComposites(instance, params)
        if (instance.save()) {
            render "1"
        } else {
            render instance.errors.allErrors.collect { g.message(error: it) } as JSON
        }
    }

    def delete = {
        try {
            DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(params.domainClass)
            def instance = domainClass.clazz.findById(params.id)
            def hasDeleted = domainClass.hasPersistentProperty("deleted")
            if (hasDeleted) {
                instance.deleted = true
                instance.save()
            } else {
                instance.delete(flush: true)
            }
            render "1"
        } catch (e) {
            render message(code: 'default.not.deleted.message')
        }
    }

    def treeStructure = {
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(params.domainClass)
        def relationProperty
        if (params.relationProperty)
            relationProperty = domainClass.properties.find() { it.name.toLowerCase() == params.relationProperty.toLowerCase() }
        else
            relationProperty = domainClass.properties.find() { it.domainClass == domainClass }

        def titleProperty
        if (params.titleProperty)
            titleProperty = domainClass.properties.find() { it.name.toLowerCase() == params.titleProperty.toLowerCase() }
        else
            titleProperty = domainClass.properties.find() { it.name.toLowerCase() == 'name' }

        def selectedIds = []
        if (params.selected)
            selectedIds = params.selected.split(',').collect { it.toLong() }

//        def recordList = domainClass.clazz.findAll()

        def openIds = []
        selectedIds.each { selectedId ->
            def id = selectedId
            def list = domainClass.clazz.createCriteria().list {
                eq("deleted", false)
                eq("id", id)
            }
            def currentParentId = list.size() > 0 ? list.first()."${relationProperty.name}Id" : null
            while (currentParentId) {
                openIds << currentParentId
                id = currentParentId
                currentParentId = domainClass.clazz.createCriteria().list {
                    eq("deleted", false)
                    eq("id", id)
                }.first()."${relationProperty.name}Id"
            }
        }

        def structure
        if (params.id)
            structure = fillRecordChildren([id: params.id.toLong()], domainClass, relationProperty, titleProperty, selectedIds, openIds)
        else
            structure = fillRecordChildren(null, domainClass, relationProperty, titleProperty, selectedIds, openIds)

        render structure as grails.converters.JSON

    }

    def fillRecordChildren(root, domainClass, relationProperty, titleProperty, selectedIds, openIds) {
        def recordList
        if (root)
            recordList = domainClass.clazz.createCriteria().list {
                eq("deleted", false)
                eq("${relationProperty.name}.id", root.id)
            }.collect { [id: it.id, text: it.properties[titleProperty.name], checked: selectedIds.contains(it.id), state: (openIds.contains(it.id) ? 'open' : 'closed'), children: []] }
        else
            recordList = domainClass.clazz.createCriteria().list {
                eq("deleted", false)
                isNull(relationProperty.name)
            }.collect { [id: it.id, text: it.properties[titleProperty.name], checked: selectedIds.contains(it.id), state: (openIds.contains(it.id) ? 'open' : 'closed'), children: []] }


        recordList.each {
            it.children = fillRecordChildren(it, domainClass, relationProperty, titleProperty, selectedIds, openIds)
            if (it.children == [])
                it.state = 'open'
        }

        return recordList
    }

}
