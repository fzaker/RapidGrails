package rapidgrails

import ar.com.fdvs.dj.domain.DJCalculation
import java.text.Format
import java.text.DecimalFormat

/**
 * Author: Farshid Zaker
 * Shayen Information Techology
 * 1/15/11 - 3:23 PM
 */

class Field {
    String title
    String column
    String className
    String name
    int width
    Format format
    DJCalculation footerCalculation

    static getColumn(fields, title) {
        def result = null
        fields.each {
            if (it.title == title) {
                result = it.column
            }
        }
        result
    }

    static DecimalFormat DefaultDecimalFormat = new DecimalFormat("###,###")

    static String(params) {
        def field = new Field(title: params.name, column: params.name, className: String.class.getName(), width: params.width ?: 50)
        field
    }

    static Integer(params) {
        def field = new Field(title: params.name, column: params.name, className: Integer.class.getName(), width: params.width ?: 50)
        field
    }

    static Long(params) {
        def field = new Field(title: params.name, column: params.name, className: Long.class.getName(), width: params.width ?: 50)
        field
    }

    static Double(params) {
        def field = new Field(title: params.name, column: params.name, className: Double.class.getName(), width: params.width ?: 50, format: params.format ?: DefaultDecimalFormat, footerCalculation: params.footerCalculation)
        field
    }
}
