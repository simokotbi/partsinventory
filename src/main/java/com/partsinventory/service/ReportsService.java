package com.partsinventory.service;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.partsinventory.configuration.DbConnection;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class ReportsService {

    private static ReportsService instance = new ReportsService();
    private Map<String, Object> parameters = new HashMap<>();

    private ReportsService() {
    }

    public static ReportsService getInstance() {
        return instance;
    }

    public void resetParameters() {
        parameters = new HashMap<>();
    }

    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    public void generatePartReport(String template, String reportFile) {
        try (Connection connection = DbConnection.getConnection()) {
            File reportFolder = new File(System.getProperty("user.home"), "reports");
            reportFolder.mkdirs();
            JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream("/templates/" + template));
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            JasperExportManager.exportReportToPdfFile(jasperPrint, reportFolder.getAbsolutePath() + "/" + reportFile);
            System.out.println("Part report generated successfully.");
        } catch (SQLException | JRException e) {
            e.printStackTrace();
        }
    }
}
