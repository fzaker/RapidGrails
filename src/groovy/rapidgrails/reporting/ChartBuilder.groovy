package rapidgrails.reporting

/**
 * Created by IntelliJ IDEA.
 * User: Zaker
 * Date: 2/3/12
 * Time: 7:09 PM
 * To change this template use File | Settings | File Templates.
 */

class ChartBuilder extends BuilderSupport {
    private charts = []

    @Override
    protected void setParent(Object parent, Object child) {
        ((Chart)parent).columns << child
    }

    @Override
    protected Object createNode(Object name) {
        if (name == "chart") {
            def chart = new Chart()
            charts << chart
            return chart
        }
        if (name == "column")
            return new Column()
    }

    @Override
    protected Object createNode(Object name, Object value) {
        if (name == "chart") {
            def chart = new Chart(title: value)
            charts << chart
            return chart
        }
        if (name == "column")
            return new Column(title: value)
    }

    @Override
    protected Object createNode(Object name, Map attributes) {
        if (name == "chart") {
            def chart = new Chart(attributes)
            charts << chart
            return chart
        }
        if (name == "column")
            return new Column(attributes)
    }

    @Override
    protected Object createNode(Object name, Map attributes, Object value) {
        throw new Exception("I don't care about this!")
    }

    def getCharts() {
        charts
    }
}

