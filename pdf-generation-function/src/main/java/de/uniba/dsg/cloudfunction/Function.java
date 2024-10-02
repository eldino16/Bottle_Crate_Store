package de.uniba.dsg.cloudfunction;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.lowagie.text.DocumentException;

import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import com.google.cloud.firestore.Firestore;

@Slf4j
public class Function implements HttpFunction {
    Firestore db = initializeDB();
    //********First we set up the firestore and initialize it**************
    private static Firestore initializeDB() {
        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.fromStream(new FileInputStream("src/main/resources/firebase/beverage-store-group15-088d0cfcb705.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).setProjectId("beverage-store-group15").build();
        FirebaseApp.initializeApp(options);

        return FirestoreClient.getFirestore();
    }

    //************************************************************************
    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        Order receivedOrder = parseOrder(request);
        System.out.println(receivedOrder);
        String pdfFileName = "Order_Number_" + receivedOrder.getId() + "_Receipt" + ".pdf";
        String generatedHtml = parseThymeTemplate(receivedOrder);
        byte[] pdf = convertHtmlToPdf(generatedHtml);
        storePDFtoCloud(pdfFileName, pdf, receivedOrder.getUserEmail());
        storeUsageToFirestore(receivedOrder, db);
        response.getWriter().write("order is saved in the storage and firebase statistic and email has been sent");
    }

    private Order parseOrder(HttpRequest request) throws IOException {
        return new ObjectMapper().readValue(request.getReader().lines().collect(Collectors.joining()), Order.class);
    }

    public String parseThymeTemplate(Order receivedOrder) {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        Context context = new Context();
        context.setVariable("order", receivedOrder);
        return templateEngine.process("shamim.html", context);
    }

    public byte[] convertHtmlToPdf(String html) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            log.info("Pdf created successfully");
            return outputStream.toByteArray();
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            log.info("Pdf creation FAILED");
        }
        return null;
    }

    private void storePDFtoCloud(String pdfFileName, byte[] data, String customerEmail) {
        try {
            String projectId = "beverage-store-group15";
            String bucketName = "pdf_order";
            Map<String, String> newMetadata = new HashMap<>();
            newMetadata.put("email", customerEmail);
            StorageOptions storageOptions = StorageOptions.newBuilder().setProjectId(projectId).setCredentials(GoogleCredentials.fromStream(new FileInputStream("src/main/resources/beverage-store-group15-fe00a4e2e557.json"))).build();
            Storage storage = storageOptions.getService();
            BlobId blobId = BlobId.of(bucketName, pdfFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setMetadata(newMetadata).setContentType("application/pdf").build();
            storage.create(blobInfo, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void storeUsageToFirestore(Order order, Firestore db) {
        DocumentReference docRef = db.collection("Orders").document(String.valueOf(order.getId()));
        Map<String, Object> data = new HashMap<>();
        data.put("Delivery_PLZ", order.getPostalCode());
        data.put("TimeStamp_Of_Order", order.getTimeStamp());
        for (int i = 0; i < order.getListOfItems().size(); i++) {
            data.put("beverageName_" + i + "", order.getListOfItems().get(i).getBeverageName());
            data.put("beverageID_" + i + "", order.getListOfItems().get(i).getBeverageId());
            data.put("beverageQuantity_" + i + "", order.getListOfItems().get(i).getQuantity());
        }
        ApiFuture<WriteResult> result = docRef.set(data);
        System.out.println(result);
    }
}