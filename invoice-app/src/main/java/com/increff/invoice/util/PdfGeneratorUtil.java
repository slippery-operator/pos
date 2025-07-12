package com.increff.invoice.util;

import com.increff.invoice.model.form.OrderRequest;
import org.apache.fop.apps.*;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.stereotype.Component;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for PDF generation using Apache FOP and Velocity templates
 * Generates invoice PDFs from order data
 */
@Component
public class PdfGeneratorUtil {

    private static final String INVOICE_TEMPLATE_PATH = "templates/invoice_template.vm";
    private static final String FOP_CONFIG_PATH = "fop-config.xml";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    private VelocityEngine velocityEngine;
    private FopFactory fopFactory;

    public PdfGeneratorUtil() {
        initializeVelocityEngine();
        initializeFopFactory();
    }

    /**
     * Generate PDF from order request and return as base64 string
     */
    public String generatePdfAsBase64(OrderRequest orderRequest) throws Exception {
        // Generate XML content from Velocity template
        String xmlContent = generateXmlFromTemplate(orderRequest);
        
        // Convert XML to PDF using FOP
        byte[] pdfBytes = convertXmlToPdf(xmlContent);
        
        // Convert to base64 string
        return Base64.getEncoder().encodeToString(pdfBytes);
    }

    /**
     * Generate XML content from Velocity template
     */
    private String generateXmlFromTemplate(OrderRequest orderRequest) throws Exception {
        Template template = velocityEngine.getTemplate(INVOICE_TEMPLATE_PATH);
        VelocityContext context = createVelocityContext(orderRequest);
        
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        
        return writer.toString();
    }

    /**
     * Create Velocity context with order data
     */
    private VelocityContext createVelocityContext(OrderRequest orderRequest) {
        VelocityContext context = new VelocityContext();
        
        // Add order details
        context.put("orderId", orderRequest.getOrderId());
        
        // Convert UTC Instant to IST and format
        String formattedOrderTime = formatZonedDateTimeToIST(orderRequest.getOrderTime());
        context.put("orderTime", formattedOrderTime);
        
        context.put("clientName", orderRequest.getClientName());
        context.put("totalRevenue", String.format("%.2f", orderRequest.getTotalRevenue()));
        
        // Add order items
        context.put("orderItems", orderRequest.getOrderItems());

        // Current date in IST
        String currentDateIST = ZonedDateTime.now(IST_ZONE).format(DATE_FORMATTER);
        context.put("currentDate", currentDateIST);
        
        return context;
    }

    private String formatZonedDateTimeToIST(String zonedDateTimeStr) {
        if (zonedDateTimeStr == null || zonedDateTimeStr.trim().isEmpty()) {
            return "N/A";
        }
        ZonedDateTime zdt = ZonedDateTime.parse(zonedDateTimeStr);
        ZonedDateTime istTime = zdt.withZoneSameInstant(IST_ZONE);
        return istTime.format(DATE_FORMATTER);
    }

    /**
     * Convert XML content to PDF using FOP
     */
    private byte[] convertXmlToPdf(String xmlContent) throws Exception {
        // Create FOP instance
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        // Create output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // Create FOP
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, outputStream);
        
        // Create transformer
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        
        // Create source and result
        Source source = new StreamSource(new StringReader(xmlContent));
        Result result = new SAXResult(fop.getDefaultHandler());
        
        // Transform XML to PDF
        transformer.transform(source, result);
        
        return outputStream.toByteArray();
    }

    /**
     * Initialize Velocity engine
     */
    private void initializeVelocityEngine() {
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
    }

    /**
     * Initialize FOP factory
     */
    private void initializeFopFactory() {
        try {
            // Load FOP configuration from classpath
            InputStream configStream = getClass().getClassLoader().getResourceAsStream(FOP_CONFIG_PATH);
            if (configStream != null) {
                fopFactory = FopFactory.newInstance(new File(".").toURI(), configStream);
            } else {
                // Use default configuration if custom config not found
                fopFactory = FopFactory.newInstance(new File(".").toURI());
            }
        } catch (Exception e) {
            // Fallback to default configuration
            fopFactory = FopFactory.newInstance(new File(".").toURI());
        }
    }
} 