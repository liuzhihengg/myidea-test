package javabase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MemoryInfoAnalyzer {

    public static void main(String[] args) {
        try {
            File inputFile = new File("/Users/tiaojiheng/study/sshgcc/malloc_info.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList heapList = doc.getElementsByTagName("heap");
            ArrayList<HeapInfo> heaps = new ArrayList<>();

            for (int temp = 0; temp < heapList.getLength(); temp++) {
                Node nNode = heapList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    int heapNr = Integer.parseInt(eElement.getAttribute("nr"));
                    int currentSize = Integer.parseInt(eElement.getElementsByTagName("system").item(0).getAttributes().getNamedItem("size").getTextContent());
                    int maxSize = Integer.parseInt(eElement.getElementsByTagName("system").item(1).getAttributes().getNamedItem("size").getTextContent());
                    heaps.add(new HeapInfo(heapNr, currentSize, maxSize));
                }
            }

            Collections.sort(heaps, Comparator.comparingInt(HeapInfo::getCurrentSize).reversed());

            int sum = 0;
            int max = 0;
            for (HeapInfo heap : heaps) {
                System.out.printf("Heap Number: %d - Current Size: %d KB, Max Size: %d KB, Remaining: %d KB\n",
                        heap.getHeapNr(), heap.getCurrentSize() / 1024, heap.getMaxSize() / 1024, (heap.getMaxSize() - heap.getCurrentSize()) / 1024);
                sum += heap.getCurrentSize() / 1024;
                max += heap.getMaxSize() / 1024;
            }
            System.out.println("Sum: " + sum + "KB");
            System.out.println("Max: " + max + "KB");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class HeapInfo {
        private int heapNr;
        private int currentSize;
        private int maxSize;

        public HeapInfo(int heapNr, int currentSize, int maxSize) {
            this.heapNr = heapNr;
            this.currentSize = currentSize;
            this.maxSize = maxSize;
        }

        public int getHeapNr() {
            return heapNr;
        }

        public int getCurrentSize() {
            return currentSize;
        }

        public int getMaxSize() {
            return maxSize;
        }
    }
}


