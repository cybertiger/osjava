package org.osjava.reportrunner.servlets;

import java.io.*;
import java.util.Date;
import javax.servlet.http.*;
import org.osjava.reportrunner.*;

public class ReportRunnerServlet extends HttpServlet {

    public static final String GROUP = "_group";
    public static final String REPORT = "_report";
    public static final String RENDERER = "_renderer";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String groupName = request.getParameter(GROUP);
        String reportName = request.getParameter(REPORT);
        String rendererName = request.getParameter(RENDERER);

        // which report they want
        Report report = ReportFactory.getReport(groupName, reportName);
        applyResources(report, request);

        // does report require parameters?
        Param[] params = report.getParams();
        if(params != null) {
            for(int i=0; i<params.length; i++) {
                Parser parser = params[i].getParser();
                String parameter = request.getParameter(params[i].getName());
                Object value = parameter;
                if(parser != null) {
                    value = parser.parse(parameter, params[i].getType());
                } 
                params[i].setValue(value);
            }
        }

        Renderer renderer = ReportFactory.getRenderer(rendererName);

        // prepare response
        response.setContentType( renderer.getMimeType() );
        if(!renderer.isInline()) {
            response.setHeader("Content-Disposition", "attachment; filename="+report.getName()+"."+renderer.getExtension());
        }

        // render results
        if(renderer != null && report != null) {

            long rep_start = System.currentTimeMillis();
            Result result = report.execute();
            long rep_time = System.currentTimeMillis() - rep_start;
            
            if(result == null) {
                throw new RuntimeException("Result is null. ");
            }
            result = new FormattingResult(result, report);

            long rend_start = System.currentTimeMillis();
            renderer.display( result, report, response.getOutputStream() );
            long rend_time = System.currentTimeMillis() - rend_start;

            logReport(report, request, rep_time, rend_time);
        } else {
            throw new RuntimeException("Renderer or Report is null. ");
        }

        response.getOutputStream().flush();

    }

    private static synchronized void logReport(Report report, HttpServletRequest request, long report_time, long render_time) {
        FileWriter writer = null;
        try {
            writer = new FileWriter( new File("rrr.log") );
            // add more to this
            StringBuffer buffer = new StringBuffer();
            buffer.append(new Date());
            buffer.append(",");
            buffer.append(report_time);
            buffer.append(",");
            buffer.append(render_time);
            buffer.append(",");
            buffer.append(report.getName());
            writer.write(buffer.toString());
            writer.flush();
            writer.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if(writer != null) { try { writer.close(); } catch(IOException ioe) { } }
        }
    }

    public static void applyResources(Report report, HttpServletRequest request) {
        String[] required = report.getResourceNames();
        for(int i=0; i<required.length; i++) {
            String value = request.getParameter(required[i]);
            if(value != null && !value.equals("")) {
                report.setResource(required[i], request.getParameter(required[i]));
            }
        }
    }
}
