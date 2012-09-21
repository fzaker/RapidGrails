import org.hibernate.dialect.SQLServer2008Dialect;

import java.sql.Types;

/**
 * Created by IntelliJ IDEA.
 * User: Zaker
 * Date: 2/22/12
 * Time: 3:22 PM
 * To change this template use File | Settings | File Templates.
 */

public class UnicodeSQLServerDialect extends SQLServer2008Dialect {
    public UnicodeSQLServerDialect() {
        super();

        // Use Unicode Characters
        registerColumnType(Types.VARCHAR, 255, "nvarchar($l)");
        //registerColumnType(Types.CHAR, "nchar(1)");
        //registerColumnType(Types.CLOB, "nvarchar(max)");
    }
}
