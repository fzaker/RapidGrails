package rapidgrails

class CompositeHelper {
    def bindComposites(instance, params) {
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
                    if (!parentPropertyName)
                        parentPropertyName = o.belongsTo.keySet().find()
                    o."${parentPropertyName}" = instance
                }
            }
        }
    }
}
