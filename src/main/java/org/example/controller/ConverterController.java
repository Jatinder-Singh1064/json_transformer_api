package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import org.example.model.UserEntity;
import org.example.service.ApiKeyService;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ConverterController {

    private final ApiKeyService apiKeyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConverterController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping("/json-to-csv")
    public String convertJsonToCsv(@RequestHeader(value = "X-API-KEY", required = false) String apiKey,
                                   @RequestBody String jsonInput) {

//        System.out.println("Controller received key: " + apiKey);

        if (apiKey == null) {
            return "Controller says: Key is missing from headers!";
        }

        try {
            // 1. Parse the JSON input
            JsonNode rootNode = objectMapper.readTree(jsonInput);

            // Ensure we are dealing with an array of objects
            if (!rootNode.isArray()) {
                return "Error: Input must be a JSON Array of objects.";
            }

            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer);

            List<String[]> data = new ArrayList<>();

            // 2. Extract Headers from the first object
            if (rootNode.has(0)) {
                JsonNode firstObject = rootNode.get(0);
                List<String> headers = new ArrayList<>();
                firstObject.fieldNames().forEachRemaining(headers::add);
                data.add(headers.toArray(new String[0]));

                // 3. Extract Rows
                for (JsonNode node : rootNode) {
                    List<String> row = new ArrayList<>();
                    for (String header : headers) {
                        row.add(node.get(header).asText());
                    }
                    data.add(row.toArray(new String[0]));
                }
            }

            csvWriter.writeAll(data);
            csvWriter.close();

            return writer.toString();

        } catch (Exception e) {
            return "Error during conversion: " + e.getMessage();
        }
    }

    @PostMapping("/register")
    @ResponseBody
    public String register(@RequestParam("email") String email) {
//        System.out.println("Email received to register : " + email);
        return "Your New API Key: " + apiKeyService.generateNewKey(email);
    }

    @GetMapping("/admin/usage")
    public List<UserEntity> getUsageReport() {
        // In a real app, you would add a check here to ensure
        // only YOU (the admin) can see this.
        return apiKeyService.getAllUsageReports();
    }
}
