package rapidgrails

class CompositeHelper {
    def bindComposites(instance, params) {
        if (!instance.hasProperty("composites"))
            return
        def composites = instance.composites
        if (!composites)
            return
        composites.each { composite ->
            def _toBeRemoved = instance."${composite}".findAll {it?.deleted || !it}
            if (_toBeRemoved) {
                instance."${composite}".removeAll(_toBeRemoved)
            }
            def parentPropertyName
            instance."${composite}".eachWithIndex() { o, i ->
                if (o) {
                    o.indx = i
//                    if (!parentPropertyName) {
//                        if (o.belongsTo instanceof Map)
//                            parentPropertyName = o.belongsTo.find {entry -> entry.value == instance.class}.key
//                        else if (o.belongsTo instanceof List)
//                            parentPropertyName = o.properties.find { it.type == instance.type }
//                    }
//                    o."${parentPropertyName}" = instance
                }
            }
        }
    }
}
