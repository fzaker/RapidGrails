package rapidgrails.reporting

/**
 * Created by IntelliJ IDEA.
 * User: Zaker
 * Date: 2/3/12
 * Time: 7:16 PM
 * To change this template use File | Settings | File Templates.
 */

class Chart {
    String title
    String subtitle
    String variable
    String yTitle
    String chartType = "bar"
    String groupBy = ""
    def columns = []
}
