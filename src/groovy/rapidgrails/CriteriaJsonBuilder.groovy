package rapidgrails

/**
 * Created with IntelliJ IDEA.
 * User: Zaker
 * Date: 8/2/12
 * Time: 3:58 PM
 * To change this template use File | Settings | File Templates.
 */

class CriteriaJsonBuilder extends BuilderSupport {
    @Override
    protected void setParent(Object parent, Object child) {
        def a = 0;
    }

    @Override
    protected Object createNode(Object name) {
        def a = 0;
    }

    @Override
    protected Object createNode(Object name, Object value) {
        def a = 0;
    }

    @Override
    protected Object createNode(Object name, Map attributes) {
        def a = 0;
    }

    @Override
    protected Object createNode(Object name, Map attributes, Object value) {
        throw new Exception("I don't care about this!")
    }
}
