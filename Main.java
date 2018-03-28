import java.io.*;
import com.mysql.jdbc.*;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Main {
    static String pwd;
    protected static final Browser browser;
    protected static BrowserView view;

    static {
        pwd = System.getProperty("user.dir");
        browser = new Browser();
        view = new BrowserView(browser);
        browser.addScriptContextListener(new ScriptContextAdapter() {
            @Override
            public void onScriptContextCreated(ScriptContextEvent event) {
                Browser browser = event.getBrowser();
                JSValue window = browser.executeJavaScriptAndReturnValue("window");
                window.asObject().setProperty("java", new JavaObject());
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ReMed - Medicine Reminder Application");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(view, BorderLayout.CENTER);
        frame.setSize(1400, 880);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        System.out.println(pwd);
        browser.loadURL("file://"+ pwd +"/GUI/main.html");
    }

    public static class JavaObject extends ReMed{}
}

interface session {
    int session_id = Integer.parseInt(File.fetch("/config/details.txt"));
}

class ReMed extends Main implements session{
    int sess_id = Integer.parseInt(File.fetch("/config/details.txt"));

    public void print(String msg){
        System.out.println(msg);
    }

    public boolean action(int id, String reason){
        System.out.println("edit medicine called");
        MissedMedicine medicine = new MissedMedicine(id, reason);
        medicine.updateReason();
        return true;
    }

    public boolean print1(int id, String reason){
        System.out.println("edit reason called");
        MissedMedicine medicine = new MissedMedicine(id, reason);
        medicine.updateReason();
        return true;
    }

    public boolean print2(int id, int taken){
        System.out.println("taken called");
        TakenMedicine medicine = new TakenMedicine(id,taken);
        medicine.updateTaken();
        return true;
    }

    public int print3() {
        sess_id = Integer.parseInt(File.fetch("/config/details.txt"));
        try{
            boolean s = DB.update("insert into ReMed.Medicines values(null,'', 99, 'Daily', 'Tablet', 99, " + sess_id + ", 0)");
            ResultSet rs = DB.fetch("select id from ReMed.Medicines where time=99 and amount=99;");
            rs.next();
            int id = rs.getInt("id");
            System.out.println("called..." + id);
            return id;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }

    }

    public int print5(String name, int age, String email, String password) {
        try{
            boolean s = DB.update("insert into ReMed.Users values(null, '" + name +"',"+ age +",'"+ email +"', '" + password +"');");
            ResultSet rs = DB.fetch("select id from ReMed.Users where email='" + email + "' and password='"+password +"';");
            rs.next();
            int id = rs.getInt("id");
            sess_id = id;
            File.write("/config/details.txt", Integer.toString(sess_id));
            System.out.println("called..." + id);
            return id;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }

    }

    public boolean print4(){
        boolean b = File.write("/config/details.txt", "-1");
        Main.browser.loadURL("file://"+ Main.pwd +"/GUI/main.html");
        return b;
    };

    public boolean action2(){ // userAuth session
        int id =Integer.parseInt(File.fetch("/config/details.txt"));
        User user = DB.fetchUser("select * from ReMed.users where id="+id);
        return user!=null;
    }
    public boolean action3(String email, String password){ // userAuth DB
        User user = DB.fetchUser("select * from ReMed.users where email='" +
                                    email + "' and password='" + password + "';");
        if(user!=null){
            user.saveSession();
        }
        return user!=null;
    }
    public JSObject action4(JSObject obj){
        System.out.println("asdfghj");
        int id =  Integer.parseInt(File.fetch("/config/details.txt"));
        User user = DB.fetchUser("select * from ReMed.users where id="+id);
        JSContext js = obj.getContext();
        JSValue result = browser.executeJavaScriptAndReturnValue("[1]");
        JSArray array = result.asArray();

        String query = "SELECT Medicines.id, medicineName, time, type, amount, frequency, taken FROM ReMed.Medicines INNER JOIN ReMed.Users on ReMed.Medicines.user_id = ReMed.Users.id where ReMed.Users.id = " + id;
        Medicine[] medicines = DB.fetchMedicines(query);
        int i=0;

        while(medicines[i]!=null){
            JSObject temp = js.createObject();
            temp.setProperty("id",medicines[i].id);
            temp.setProperty("medicineName",medicines[i].medicineName);
            temp.setProperty("frequency",medicines[i].frequency);
            temp.setProperty("time",medicines[i].time);
            temp.setProperty("amount",medicines[i].amount);
            temp.setProperty("type",medicines[i].type);
            temp.setProperty("taken",medicines[i].took);
            array.set(i,temp);
            i++;
        }

        obj.setProperty("medicines", array);

        result = browser.executeJavaScriptAndReturnValue("[]");
        array = result.asArray();

        query = "SELECT Medicines.id, MissedMedicines.id, medicineName, time, type, amount, frequency, date, reason FROM ReMed.Medicines INNER JOIN ReMed.MissedMedicines on ReMed.Medicines.id = ReMed.MissedMedicines.medicine_id where MissedMedicines.user_id = " + id;
        MissedMedicine[] missedMedicines = DB.fetchMissedMedicines(query);
        i=0;

        while(missedMedicines[i]!=null){
            JSObject temp = js.createObject();
            temp.setProperty("id",missedMedicines[i].id);
            temp.setProperty("medicineName",missedMedicines[i].medicineName);
            temp.setProperty("frequency",missedMedicines[i].frequency);
            temp.setProperty("time",missedMedicines[i].time);
            temp.setProperty("amount",missedMedicines[i].amount);
            temp.setProperty("type",missedMedicines[i].type);
            temp.setProperty("date",missedMedicines[i].missedDate);
            temp.setProperty("reason",missedMedicines[i].reason);
            temp.setProperty("mid",missedMedicines[i].mid);
            array.set(i,temp);
            i++;
        }

        obj.setProperty("missedMedicines",array);

        obj.setProperty("user_id", id);

        return obj;
    }

    public boolean action5(int id, String medicineName, int amount, String type, String frequency, int time){ // edit medicine
        System.out.println("edit medicine called");
        Medicine medicine = new Medicine(amount, medicineName, time, frequency, type, id);
        return medicine.updateDB();
    }

}

class Medicine{
    private static String[] frequency_options;
    private static String[] type_options;

    static {
        frequency_options = new String[]{"Daily", "Weekly", "Montly"};
        type_options = new String[]{"Tablet", "Capsule", "Sache"};
    }

    Integer amount;
    String  medicineName;
    Integer time;
    String  frequency;
    String  type;
    Integer id;
    int took;

    Medicine(){

    }

    Medicine(Integer amount, String medicineName, Integer time, String frequency, String type, Integer id){
        this.amount = amount;
        this.medicineName = medicineName;
        this.time = time;
        this.frequency = frequency;
        this.type = type;
        this.id = id;

    }

    Medicine(Integer amount, String medicineName, Integer time, String frequency, String type, Integer id, int took){
        this.amount = amount;
        this.medicineName = medicineName;
        this.time = time;
        this.frequency = frequency;
        this.type = type;
        this.id = id;
        this.took = took;
    }

    Medicine(Integer amount, String medicineName, Integer time, String frequency, String type){
        this.amount = amount;
        this.medicineName = medicineName;
        this.time = time;
        this.frequency = frequency;
        this.type = type;
        writeDB();
    }

    int getAmount(){
        return this.amount;
    }
    String getMedicineName(){
        return this.medicineName;
    }
    Integer getTime(){
        return this.time;
    }
    String getFrequency(){
        return this.frequency;
    }
    String getType(){
        return this.type;
    }

    Medicine setAmount(int amount){
        this.amount = amount;
        updateDB();
        return this;
    }
    Medicine setMedicineName(String medicineName){
        this.medicineName = medicineName;
        updateDB();
        return this;
    }
    Medicine setTime(Integer time){
        this.time = time;
        updateDB();
        return this;
    }

    boolean updateDB() {
        boolean success = DB.update(
                "update ReMed.Medicines set "
                + "medicineName='" + this.medicineName + "', "
                + "time=" + this.time + ", "
                + "frequency='" + this.frequency + "', "
                + "type='" + this.type + "', "
                + "amount=" + this.amount + " "
                + "where id=" + this.id + ";"
        );
        return success;
    }
    void writeDB() {

    }
}

class MissedMedicine extends Medicine{
    String missedDate;
    String reason;
    Integer mid;

    MissedMedicine(){

    }

    MissedMedicine(Integer amount, String medicineName, Integer time, String frequency,
                        String type, Integer mid, String missedDate, String reason, Integer id){
        super(amount, medicineName, time, frequency, type, id);
        this.missedDate = missedDate;
        this.reason = reason;
        this.mid = mid;
    }

    MissedMedicine(Integer id, String reason){
        this.id = id;
        this.reason = reason;
    }

    boolean updateReason(){
        return DB.update(
                "update ReMed.MissedMedicines set "
                + "reason = '" + this.reason + "' where "
                + "id = " + this.id + ";"
        );
    }
}

class TakenMedicine extends Medicine{
    int taken;

    TakenMedicine(){

    }
    TakenMedicine(int med_id, int taken_flag){
        this.id = med_id;
        this.taken = taken_flag;
    }

    boolean updateTaken(){
        boolean success = DB.update(
                "update ReMed.Medicines set "
                        + "taken= " + this.taken + " "
                        + "where id= " + this.id + ";"
        );
        return success;
    }
}

class DB{
    private static String dbUrl = "jdbc:mysql://127.0.0.1:3306/";
    private static String dbUsername = "root";
    private static String dbPassword = "root";
    private static Connection connection;

    static{
        try{
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch(SQLException e) {
            System.out.println("SQL Exception >> ");
            e.printStackTrace();
        } catch(Exception e) {
            System.out.println("Exception >> ");
            e.printStackTrace();
        }
    }

    static boolean update(String query){
        try{
            Statement s = connection.createStatement();
            int success = s.executeUpdate(query);
            return success==1;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    static Medicine[] fetchMedicines(String query){
        try{
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery(query);
            rs.last();
            int rows = rs.getRow();
            rs.beforeFirst();
            Medicine[] medicines = new Medicine[rows+1];
            int i=0;
            while(rs.next()){
                int id = rs.getInt("id");
                int amount = rs.getInt("amount");
                String medicineName = rs.getString("medicineName");
                int time = rs.getInt("time");
                String frequency = rs.getString("frequency");
                String type = rs.getString("type");
                int took = rs.getInt("taken");

                medicines[i] = new Medicine(amount, medicineName, time, frequency, type, id, took);
                i++;
            }
            return medicines;
        }catch(SQLException e){
            e.printStackTrace();
            return (new Medicine[0]);
        }
    }

    static MissedMedicine[] fetchMissedMedicines(String query){
        try{
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery(query);
            rs.last();
            int rows = rs.getRow();
            rs.beforeFirst();
            MissedMedicine[] medicines = new MissedMedicine[rows+1];
            int i=0;
            while(rs.next()){
                int mid = rs.getInt("Medicines.id");
                int amount = rs.getInt("amount");
                String medicineName = rs.getString("medicineName");
                int time = rs.getInt("time");
                String frequency = rs.getString("frequency");
                String type = rs.getString("type");
                String date = rs.getString("date");
                String reason = rs.getString("reason");
                int id = rs.getInt("MissedMedicines.id");

                medicines[i] = new MissedMedicine(amount, medicineName, time, frequency, type, mid, date, reason, id);
                i++;
            }
            if(i==0){
                medicines[0]=null;
            }
            return medicines;
        }catch(SQLException e){
            e.printStackTrace();
            MissedMedicine[] medicines = new MissedMedicine[1];
            medicines[0]=null;
            return (medicines);
        }
    }

    static User fetchUser(String query){
        try{
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery(query);

            if(rs.next()){
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age  = rs.getInt("age");
                String email = rs.getString("email");
                return new User(id, name, age, email);
            }
            else{
                return null;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    static ResultSet fetch(String query)
    throws SQLException, Exception{
        Statement s = connection.createStatement();
        return s.executeQuery(query);
    }

    boolean write(String query){
        return update(query);
    }
}

class User{
    private Integer id;
    String name;
    Integer age;
    String email;

    User(){

    }
    User(Integer id, String name,Integer age, String email){
        this.id = id;
        this.name  = name;
        this.age   = age;
        this.email = email;
    }
    void saveSession(){
        System.out.println("session saved");
        File.write("/config/details.txt", (new Integer(this.id)).toString());
    }
}

class NewUser extends User{

    private String password;
    private String username;

    NewUser(){

    }
    NewUser(Integer id, String name,Integer age, String email, String username, String password){
        super( id, name, age, email );
    }

    boolean writeDB(){
       return DB.update("INSERT INTO ReMed.Users VALUES(NULL','" + name + "','" + age + "','" + email + "');");
    }
}

class File{

    static String fetch (String path){
        String file = Main.pwd + path;
        String line = "";

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            line = bufferedReader.readLine();
            bufferedReader.close();

            return line;
        }
        catch(FileNotFoundException e) {
            System.out.println(
                    "Unable to open file '" +
                            file + "'");
            e.printStackTrace();
            return "";
        }
        catch(IOException e) {
            System.out.println(
                    "Error reading file '"
                            + file + "'");
            e.printStackTrace();
            return "";
        }
    }
    static boolean write (String path, String content){
        String file = Main.pwd + path;
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(content);
            bufferedWriter.close();
            return true;
        }
        catch(IOException e) {
            System.out.println(
                    "Error writing to file '"
                            + file + "'");
            e.printStackTrace();
            return false;
        }
    }
}

interface Encryption{
    String public_key = "y";
    String private_key = "z";

    String encrypt(String data);
    String decrypt(String data);
}

class RSA_Encryption implements Encryption {

    static PublicKey pubKey;
    static PrivateKey priKey;


    public String encrypt(String data){
        return "h";
    }

    public String decrypt(String data){
        return "h";
    }

    public static void init() throws Exception {

        KeyPair keyPair = buildKeyPair();
        pubKey = keyPair.getPublic();
        priKey = keyPair.getPrivate();

//        // encrypt the message
//        byte [] encrypted = encrypt(priKey, "message");
//        System.out.println(new String(encrypted));
//
//        // decrypt the message
//        byte[] secret = decrypt(pubKey, encrypted);
//        System.out.println(new String(secret));
    }

    public static void initKeys(){
        java.io.File pub = new java.io.File(Main.pwd + "/config/public.txt");
        java.io.File pri = new java.io.File(Main.pwd + "/config/private.txt");

        if(pub.exists() && pri.exists()){
            try {
                byte[] publicBytes = Base64.getDecoder().decode(File.fetch("/config/public.txt")) ;
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey pubKey = keyFactory.generatePublic(keySpec);
                System.out.println(new String(publicBytes));
            }
            catch(Exception e){
    e.printStackTrace();
            }
        }
        else{
            try {
                init();
            } catch (Exception e) {
                e.printStackTrace();
            }
            java.io.File f = new java.io.File(Main.pwd + "/config/private.txt");
            java.io.File g = new java.io.File(Main.pwd + "/config/public.txt");
            try {
                f.createNewFile();
                g.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File.write( "/config/private.txt", priKey.toString());
            File.write( "/config/public.txt", pubKey.toString());

            System.out.println(" private : " + priKey.toString());
            System.out.println(" public : " + pubKey.toString());
        }
    }

    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 2048;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    public static byte[] encrypt(PrivateKey privateKey, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        return cipher.doFinal(message.getBytes());
    }

    public static byte[] decrypt(PublicKey publicKey, byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        return cipher.doFinal(encrypted);
    }

    public static KeyPair buildKeyPair2() throws NoSuchAlgorithmException {
        final int keySize = 2048;
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//        keyPairGenerator.initialize(keySize);
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//        keyPairGenerator.initialize(keySize);

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }
}






