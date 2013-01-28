package rapidgrails

import fi.joensuu.joyds1.calendar.JalaliCalendar
import grails.converters.JSON
import grails.orm.HibernateCriteriaBuilder
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import rapidgrails.reporting.ReportDataReader
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.grails.datastore.gorm.mongo.MongoCriteriaBuilder

class RapidGrailsController {
    def jsonList = {
        def max = Math.min(params.rows ? params.int('rows') : 10, 100)
        def page = params.int("page")
        def offset = (page - 1) * params.int("rows")
        def sort = params.sidx ?: null
        def order = params.sord ?: null

        def tree = params.tree
        def list = true
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
                if (params.filter) {
                    def filter = JSON.parse(params.filter)

                    def findingCriteria = { _filter, DefaultGrailsDomainClass _domainClass ->
                        def closure = {
                            def aliases = [:]
                            _filter.each { f ->
                                if (!f.field && f.op && f.data) {
                                    "${f.op}" {
                                        Closure<?> innerClosure = getFindingCriteria(f.data, _domainClass)
                                        innerClosure.setResolveStrategy(Closure.DELEGATE_ONLY)
                                        innerClosure.setDelegate(delegate)
                                        innerClosure.call()
                                    }
                                } else {
                                    def aliasFieldParts = f.field.split(/\./)
                                    def beforeDot = aliasFieldParts[0]
                                    def v
                                    if (f.op == "createAlias") {
                                        aliases.put(f.val, f.field)
                                        v = f.val
                                    } else if (aliases.keySet().contains(beforeDot)) {
                                        def assocProperty = aliases[beforeDot]
                                        def assocClass = _domainClass.associationMap[assocProperty]
                                        def assocDomainClass = grailsApplication.getDomainClass(assocClass.name)
                                        def aliasFieldProperty = assocDomainClass.getPropertyByName(aliasFieldParts[1])
                                        v = aliasFieldProperty.type.newInstance(f.val)
                                    }
                                    else { // The simple case, f.field is direct field of the class
                                        def property
                                        try {
                                            property = _domainClass.getPropertyByName(f.field)
                                            def type = property.type
                                            if (type.toString().equals("boolean"))
                                                v = f.val as Boolean
                                            else if (type.toString().equals("int"))
                                                v = f.val as Integer
                                            else if (type == Number.class)
                                                v = f.val as Integer
                                            else
                                                v = property.type.newInstance(f.val)
                                        } catch (e) {
                                            v = f.val
                                        }

//                                        v = f.val.asType(property.type)
                                    }
                                    if(f.val)
                                        "${f.op}"(f.field, v)
                                    else
                                        "${f.op}"(f.field)
                                }
                            }
                        }
                        return closure
                    }
                    HibernateCriteriaBuilder.metaClass.getFindingCriteria = findingCriteria
                    MongoCriteriaBuilder.metaClass.getFindingCriteria = findingCriteria
                    Closure<?> c = delegate.getFindingCriteria(filter, domainClass)
                    c.setResolveStrategy(Closure.DELEGATE_ONLY)
                    c.setDelegate(delegate)
                    c.call()
                }
                if (list) {
                    if (params.sort) {
                        "${"order"}"(params.sort, params.order)
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

            instanceList = domainClass.clazz.createCriteria().list(query)
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
            domainClass.constraints.keySet().eachWithIndex {pname, index ->
                if (!excludedProperties.contains(pname) && (index < maxColumnCount)) {
                    colNames << pname
                }
            }
        }

        def binding = new Binding()
        def gs = new GroovyShell(binding)

        render([page: page.toString(), total: total, records: records.toString(),
                rows: instanceList.collect {
                    def cell = []
                    if (!params.showFirstColumn || Boolean.parseBoolean(params.showFirstColumn)) {
                        cell << it.id.toString()
                    }
                    colNames.each { col ->
                        if (expressions[col]) {
                            binding.setVariable("obj", it)
                            binding.setVariable("g", g)
                            def v = gs.evaluate("${expressions[col]}")
                            if ((v instanceof Double) || (v instanceof Float))
                                v = String.format("%.2f", v)
                            cell << v
                        } else {
                            def v = it[col]
                            if (v instanceof Date) {
                                def cal = Calendar.getInstance()
                                cal.setTime(v)
                                def jc = new JalaliCalendar(cal)
                                v = String.format("%04d/%02d/%02d", jc.getYear(), jc.getMonth(), jc.getDay())
                            } else if (v instanceof String) {
                                def code1 = "${domainClass.propertyName}.${col}.${v}"
                                def code2 = "${col}.${v}"
                                v = message(code: code1, default: message(code: code2, default: v))
                            } else if ((v instanceof Double) || (v instanceof Float)) {
                                v = String.format("%.2f", v)
                            } else if (v == null)
                                v = ""

                            cell << v.toString()
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
                    [id: it.id, cell: cell]
                }, userdata: userData] as JSON)
    }

    def jsonInstance = {
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(params.domainClass)
        JSON.use("deep") {
            render domainClass.clazz.findById(params.id) as JSON
        }
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
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(params.domainClass)
        def instance
        if (params.id)
            instance = domainClass.clazz.findById(params.id)
        else
            instance = domainClass.newInstance()
        bindData(instance, params)
        bindComposites(instance, params)
        instance.save()
        render "1"
    }

    def delete = {
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(params.domainClass)
        def instance = domainClass.clazz.findById(params.id)
        instance.delete(flush: true)
        render "1"
    }
}
