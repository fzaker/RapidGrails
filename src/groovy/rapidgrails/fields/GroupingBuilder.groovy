package rapidgrails.fields

import rapidgrails.reporting.Chart
import rapidgrails.reporting.Column

/**
 * Created with IntelliJ IDEA.
 * User: Farshid
 * Date: 11/6/12
 * Time: 10:33 PM
 * To change this template use File | Settings | File Templates.
 */
class GroupingBuilder extends BuilderSupport {
    private groupings = []

    @Override
    protected void setParent(Object parent, Object child) {
        ((Grouping)parent).groups << child
    }

    @Override
    protected Object createNode(Object name) {
        if (name == "grouping") {
            def grouping = new Grouping()
            groupings << grouping
            return grouping
        }
        if (name == "group")
            return new Group()
    }

    @Override
    protected Object createNode(Object name, Object value) {
        if (name == "grouping") {
            def grouping = new Grouping(name: value)
            groupings << grouping
            return grouping
        }
        if (name == "group")
            return new Group(title: value)
    }

    @Override
    protected Object createNode(Object name, Map attributes) {
        if (name == "grouping") {
            def grouping = new Grouping(attributes)
            groupings << grouping
            return grouping
        }
        if (name == "group")
            return new Group(attributes)
    }

    @Override
    protected Object createNode(Object name, Map attributes, Object value) {
        throw new Exception("I don't care about this!")
    }

    def getGroupings() {
        groupings
    }
}
