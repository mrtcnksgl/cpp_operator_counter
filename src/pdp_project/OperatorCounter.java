package pdp_project;

import java.io.*;
import java.util.Scanner;
import java.util.regex.*;

public class OperatorCounter {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Lütfen .cpp dosyasının tam yolunu girin: ");
		String filePath = scanner.nextLine();

		try {
			// Dosya okunuyor
			String content = readFile(filePath);
			System.out.println("Dosya başarıyla okundu.");

			// Yorum ve string içerikleri temizle
			content = removeCommentsAndStrings(content);

			// Operatörleri işle ve toplam sonuçları al
			processOperatorsInMain(content);

		} catch (IOException e) {
			System.err.println("Hata: " + e.getMessage());
		}

		scanner.close();
	}

	// Dosyayı okuyup içeriği döndürür
	private static String readFile(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			throw new FileNotFoundException("Dosya bulunamadı: " + filePath);
		}

		StringBuilder contentBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				contentBuilder.append(line).append("\n");
			}
		}
		return contentBuilder.toString();
	}

	// Yorum satırlarını ve string içerikleri temizler
	private static String removeCommentsAndStrings(String content) {
		content = content.replaceAll("/\\*.*?\\*/", ""); // Çok satırlı yorumları temizle
		content = content.replaceAll("//.*", ""); // Tek satırlı yorumları temizle
		content = content.replaceAll("\".*?\"|'.*?'", ""); // String içeriklerini temizle
		return content;
	}

	// Sadece int main() bloğu içindeki operatörleri işler
	private static void processOperatorsInMain(String content) {
		// Operatörler için regex ifadeleri
		String singleOperators = "(?<![=!])!\\b|\\+\\+|--|~";
		String doubleOperators = "(?<![+\\-/&|<>!=%])=|[-+/%&|^<>]=|<<|>>|&&|\\|\\||[<>]=?|!=|(?<!\\+|-)\\+|(?<!\\+|-)\\-|\\*=|\\*|/|%";
		String tripleOperators = "\\?.*?:";

		String[] lines = content.split("\n");

		boolean inMain = false;
		int openBraces = 0;

		int totalSingleCount = 0;
		int totalDoubleCount = 0;
		int totalTripleCount = 0;

		for (int phase = 1; phase <= 3; phase++) {
			System.out.println("\n--- Faz " + phase + " ---");

			for (int i = 0; i < lines.length; i++) {
				String line = lines[i].trim();

				// int main() başlangıcını kontrol et
				if (line.contains("int main()")) {
					inMain = true;
				}

				if (inMain) {
					if (line.contains("{"))
						openBraces++;
					if (line.contains("}"))
						openBraces--;

					// main bloğundan çıkma durumu
					if (openBraces == 0)
						inMain = false;
				}

				if (inMain && !line.isEmpty()) {
					String currentRegex = switch (phase) {
					case 1 -> singleOperators;
					case 2 -> doubleOperators;
					case 3 -> tripleOperators;
					default -> "";
					};

					int count = countMatches(line, currentRegex);
					if (count > 0) {
						System.out.println("Satır " + (i + 1) + ": " + line);
						String operatorGroup = switch (phase) {
						case 1 -> "   Tekli Operatörler";
						case 2 -> "   İkili Operatörler";
						case 3 -> "   Üçlü Operatörler";
						default -> "";
						};
						System.out.println(operatorGroup + ": " + count + "\n");

						// Operatörleri metinden temizle
						lines[i] = line.replaceAll(currentRegex, "");

						// Toplam sayıları güncelle
						if (phase == 1)
							totalSingleCount += count;
						if (phase == 2)
							totalDoubleCount += count;
						if (phase == 3)
							totalTripleCount += count;
					}
				}
			}
		}

		// Toplam sonuçları yazdır
		System.out.println("\n--- Toplam Sonuçlar ---");
		System.out.println("Toplam Tekli Operatör Sayısı: " + totalSingleCount);
		System.out.println("Toplam İkili Operatör Sayısı: " + totalDoubleCount);
		System.out.println("Toplam Üçlü Operatör Sayısı: " + totalTripleCount);
	}

	// Verilen regex desenine uygun eşleşmeleri sayar
	private static int countMatches(String content, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}
}
