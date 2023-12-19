import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class TextAnalyzer {

    public static String determine_theme(Map<String, Integer> topicScores) {
        int max_score = 0;
        String current_theme = null;

        for (Map.Entry<String, Integer> entry : topicScores.entrySet()) {
            if (entry.getValue() > max_score) {
                max_score = entry.getValue();
                current_theme = entry.getKey();
            }
        }

        return current_theme;
    }

    public static String read_text_from_file(String filePath) throws IOException {

        File file = new File(filePath);

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        String file_extension = get_file_extension(filePath);

        if (file_extension.equalsIgnoreCase("txt")) {
            return read_text_from_txt_file(filePath);
        } else if (file_extension.equalsIgnoreCase("doc")) {
            return read_text_from_doc_file(filePath);
        } else if (file_extension.equalsIgnoreCase("docx")) {
            return read_text_from_docx_file(filePath);
        } else {
            throw new UnsupportedOperationException("Unsupported file format");
        }
    }

    private static String get_file_extension(String filePath) {
        int last_dot_index = filePath.lastIndexOf('.');
        if (last_dot_index > 0) {
            return filePath.substring(last_dot_index + 1);
        }
        return "";
    }

    public static String read_text_from_txt_file(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static String read_text_from_doc_file(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            HWPFDocument document = new HWPFDocument(fis);
            WordExtractor extractor = new WordExtractor(document);
            return extractor.getText();
        }
    }

    private static String read_text_from_docx_file(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            XWPFDocument document = new XWPFDocument(fis);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    public static String[] tokenize(String text) {
        return text.toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
    }

    public static Map<String, Integer> analyze_text(String filePath, Map<String, String[]> topicKeywords) throws IOException {
        String text = read_text_from_file(filePath);
        String[] tokens = tokenize(text);

        Map<String, Integer> counter = new HashMap<>();
        for (String token : tokens) {
            counter.put(token, counter.getOrDefault(token, 0) + 1);
        }

        Map<String, Integer> topicScores = new HashMap<>();
        for (Map.Entry<String, String[]> entry : topicKeywords.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                score += counter.getOrDefault(keyword, 0);
            }
            topicScores.put(entry.getKey(), score);
        }

        return topicScores;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter the path to the text file or 'exit' to quit: ");
            String filePath = scanner.nextLine();

            if (filePath.equalsIgnoreCase("exit")) {
                System.out.println("Exiting the program. Goodbye!");
                break;
            }

            Map<String, String[]> topicKeywords = new HashMap<>();
            topicKeywords.put("Medical", new String[]{"health", "medical", "doctor", "patient", "treatment", "disease", "medicine", "diagnosis", "symptoms", "hospital", "surgery", "prescription", "nurse", "wellness", "vaccine", "therapy", "recovery", "pain", "specialist", "emergency", "appointment"});
            topicKeywords.put("Programming", new String[]{"code", "programming", "algorithm", "software", "development", "computer", "java", "python", "coding", "bug", "debugging", "syntax", "compiler", "function", "variable", "database", "API", "framework", "frontend", "backend"});
            topicKeywords.put("Finance", new String[]{"money", "investment", "finance", "business", "stocks", "market", "economic", "bank", "budget", "profit", "loss", "portfolio", "dividend", "interest", "credit"});
            topicKeywords.put("History", new String[]{"history", "war", "manuscript", "archives", "ancient", "civilization", "culture", "historical", "artifact", "revolution", "event", "era", "monument", "colonial", "dynasty", "medieval"});
            topicKeywords.put("Networks", new String[]{"network", "connection", "architecture", "internet", "communication", "protocol", "router", "data", "security", "firewall", "wireless", "bandwidth", "server", "client", "IP", "LAN", "WAN", "VPN", "cloud", "cybersecurity"});
            topicKeywords.put("Cryptography", new String[]{"encrypt", "security", "information", "cryptography", "cipher", "decryption", "key", "algorithm", "privacy", "hash", "digital", "signature", "authentication"});

            try {
                Map<String, Integer> result = analyze_text(filePath, topicKeywords);

                System.out.println("Topic Scores:");
                for (Map.Entry<String, Integer> entry : result.entrySet()) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }

                String theme = determine_theme(result);
                System.out.println("Theme of the text: " + theme);
            } catch (IOException e) {
                System.err.println("Error reading the file: " + filePath);
                System.err.println("Exception details: " + e.getMessage());
            }
        }
    }
}