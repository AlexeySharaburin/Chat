import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Client {

    private static Date data = new Date();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
    final private static String nameSettings = "settings.txt";

    public static void main(String[] args) {
        try {
            int portNumber = readPortNumber();

            InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", portNumber);

            SocketChannel socketChannel = SocketChannel.open();

            socketChannel.connect(socketAddress);

            try (Scanner scanner = new Scanner(System.in)) {
                final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);
                String inputString;
                String clientName = getClientName();
                String hiClient = clientName + " вошёл в чат";

                ByteBuffer outBuffer = ByteBuffer.wrap(hiClient.getBytes(StandardCharsets.UTF_8));
                WaitingQueue.chatList.add(outBuffer);
                socketChannel.write(WaitingQueue.chatList.poll());

                int bytesCount = socketChannel.read(inputBuffer);
                System.out.println(new String(inputBuffer.array(), 0, bytesCount, StandardCharsets.UTF_8));
                inputBuffer.clear();

                while (true) {
                    System.out.print(clientName + ", введите, пожалуйста, ваше сообщение (наберите 'ВЫХОД', чтобы покинуть чат): ");
                    inputString = scanner.nextLine();
                    if ("ВЫХОД".equals(inputString)) {
                        System.out.println(clientName + ", до новых встреч!");
                        String byClient = clientName + " покинул чат";
                        System.out.println(byClient);

                        ByteBuffer msgBuffer = ByteBuffer.wrap(byClient.getBytes(StandardCharsets.UTF_8));
                        WaitingQueue.chatList.add(msgBuffer);
                        socketChannel.write(WaitingQueue.chatList.poll());

                        break;
                    }

                    String resultString = clientName + ": " + inputString;
                    outBuffer = ByteBuffer.wrap(resultString.getBytes(StandardCharsets.UTF_8));
                    WaitingQueue.chatList.add(outBuffer);
                    socketChannel.write(WaitingQueue.chatList.poll());

                    Thread.sleep(2000);
                    bytesCount = socketChannel.read(inputBuffer);
                    System.out.println(new String(inputBuffer.array(), 0, bytesCount, StandardCharsets.UTF_8));
                    inputBuffer.clear();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                socketChannel.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int readPortNumber() {
        String portNumber = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(nameSettings))) {
            String s;
            while ((s = reader.readLine()) != null) {
                portNumber = s;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Integer.parseInt(portNumber);
    }

    public static String getClientName() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Добрый день! введите ваше имя: ");
        String name = scanner.nextLine();
        return name;
    }
}