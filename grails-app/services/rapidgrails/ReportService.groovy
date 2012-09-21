package rapidgrails

import javax.servlet.http.HttpServletResponse
import ar.com.fdvs.dj.domain.builders.*
import ar.com.fdvs.dj.domain.*
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn
import ar.com.fdvs.dj.domain.constants.GroupLayout
import ar.com.fdvs.dj.domain.entities.DJGroup
import ar.com.fdvs.dj.domain.constants.Page
import ar.com.fdvs.dj.core.DynamicJasperHelper
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager
import ar.com.fdvs.dj.output.ReportWriter
import ar.com.fdvs.dj.output.ReportWriterFactory
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.data.ListOfArrayDataSource
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource

class ReportService {
    def messageSource

    def CreateReportFile(String reportTitle, String subtitle, List fields, datasource, Object[] columns, status, HttpServletResponse response, request) {
        CreateReportFile(reportTitle, subtitle, fields, datasource, columns, status, response, request, false, [])
    }

    def CreateReportFile(String reportTitle, String subtitle, List fields, datasource, Object[] columns, status, HttpServletResponse response, request, boolean landscape) {
        CreateReportFile(reportTitle, subtitle, fields, datasource, columns, status, response, request, landscape, [])
    }

    def CreateReportFile(String reportTitle, String subtitle, List fields, datasource, Object[] columns, status, HttpServletResponse response, request, boolean landscape, groups) {
        FastReportBuilder drb = new FastReportBuilder()
        def locale = new Locale("en")
        drb.setReportLocale(locale)

        Style titleStyle = StyleManager.getTitleStyle()
        Style subtitleStyle = StyleManager.getSubtitleStyle()
        Style columnStyle = StyleManager.getColumnStyle()
        Style headerStyle = StyleManager.getHeaderStyle()
        Style oddRowStyle = StyleManager.getOddRowStyle()

        def builtColumns = [:]

        fields.reverse().each {
            def col = ColumnBuilder.getInstance().setColumnProperty(it.name ?: it.title, it.className).setTitle(messageSource.getMessage(it.title, null, it.title, locale)).setWidth(it.width).setStyle(columnStyle).setHeaderStyle(headerStyle).setTextFormatter(it.format).build()
            drb = drb.addColumn(col)
            builtColumns[it.name ?: it.title] = col

            if (it.footerCalculation) {
                if (it.format) {
                    drb.addGlobalFooterVariable(col, it.footerCalculation, columnStyle)
                }
                else
                    drb.addGlobalFooterVariable(col, it.footerCalculation, columnStyle)
            }
        }

        groups.each {
            GroupBuilder gb = new GroupBuilder();
            gb = gb.setCriteriaColumn((PropertyColumn) builtColumns[it.title])
            it.aggregates.each {agg ->
                def c1 = builtColumns[agg.title]
                gb = gb.addFooterVariable(c1, agg.type, columnStyle) // tell the group place a variable footer of the column "columnAmount" with the SUM of allvalues of the columnAmount in this group.
            }
            gb = gb.setGroupLayout(GroupLayout.VALUE_IN_HEADER)
            DJGroup g = gb.build();
            drb.addGroup(g);
        }

        drb.setDefaultStyles(null, null, headerStyle, columnStyle)//.setPrintBackgroundOnOddRows(true).setOddRowBackgroundStyle(oddRowStyle)

        Page page
        int PORTRAIT_PAGE_HEIGHT = 842
        int PORTRAIT_PAGE_WIDTH = 595
        if (!landscape) {
            page = new Page(PORTRAIT_PAGE_HEIGHT, PORTRAIT_PAGE_WIDTH, true)
        } else {
            page = new Page(PORTRAIT_PAGE_WIDTH, PORTRAIT_PAGE_HEIGHT, false)
        }

        page.setOrientationPortrait(!landscape)
        DynamicReport dr = drb.setTitle(messageSource.getMessage("report." + reportTitle, null, reportTitle, locale)).setTitleStyle(titleStyle).setTitleHeight(new Integer(30)).setSubtitleHeight(new Integer(10)).setSubtitle(subtitle).setSubtitleStyle(subtitleStyle).setUseFullPageWidth(true).setPageSizeAndOrientation(page).build()//.setSubtitle(reportSubtitle).build()

        //JRDataSource ds = new ListOfArrayDataSource(datasource, columns)
        JRDataSource ds = new JRBeanCollectionDataSource(datasource)
        JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds)
        def s = status
        def reportFormat = status.toUpperCase()
        def reportFileName = reportTitle.replace(" ", "")
        ReportWriter reportWriter = ReportWriterFactory.getInstance().getReportWriter(jp, reportFormat, [(JRHtmlExporterParameter.IMAGES_URI): "${request.contextPath}/report/image?image=".toString()])
        if (reportFileName) {
            def fn = "${reportFileName}.${reportFormat.toLowerCase()}"
            response.addHeader('content-disposition', "attachment; filename=${fn}")
        }
        reportWriter.writeTo(response)
    }
}
