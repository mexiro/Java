/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dergosms;

/**
 * @author Eri
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


/**
 //https://postman-echo.com/get?foo1=bar1&foo2=bar2
 //sample
 //https://jsonplaceholder.typicode.com/posts/1/comments
 */

public class Main_Backupworking {

    public static final String DATE_FORMAT_NOW = "yy-MM-dd HH-mm-ss";

    /** for geting today's date time */
    public static String now() {

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    /** ---------------------------- */
    /** this is for parsing the HTML */
    /** ---------------------------- */
    public static String parse_html(String response) throws IOException {

        String html = "<html><head><title>First parse</title></head>"
                + "<body><p>Parsed HTML into a doc.</p></body></html>";
        Document doc = Jsoup.parse(response);
        String title = doc.title();
        //System.out.println("Title is: " + title);
        return title;
    }

    public static void main(String[] args) throws SQLException, MalformedURLException, IOException {
        // TODO code application logic here
        Connection conn = null;
        // LOG FILE INTO LOG change to V: 
        String sot = now();
        FileWriter myWriter = new FileWriter("V:\\Stock_Database\\SMSLOG\\SMSLOG "+sot+".txt");
        String newLine = System.getProperty("line.separator");


        try { /** Connect to the database */

            conn = DriverManager.getConnection("jdbc:ucanaccess://V:\\Stock_Database\\Cards_Inventory_0.20_.accdb");

        } catch (SQLException ex) {
            Logger.getLogger(DB_Link.class.getName()).log(Level.SEVERE, null, ex);
        }
        Statement st = conn.createStatement();

        ResultSet rs;
        rs = st.executeQuery("SELECT Test_R7_Datatable.Phonenr, Test_R7_Datatable.Cardholder_Name, Test_R7_Datatable.deliverydate, Test_R7_Datatable.sent, Test_R7_Datatable.SMS_LOG, Test_R7_Datatable.[card sequence no] as Pinorcrd, Test_R7_Datatable.Cardno FROM Test_R7_Datatable WHERE (((Test_R7_Datatable.deliverydate)=date()) AND ((Test_R7_Datatable.sent)=False));");
        String phone = null;
        Date senddate = null;
        Integer pinvscrd = null;
        Boolean sent = false;
        String  nrkarte =null;
        int countRows = 0; // per te numeruar Rekorder ten Resultset

        /**LOOP THROUGH RECORDS AND SEND SMS */
        while (rs.next()) {
            phone = rs.getString("phonenr");
            String crdname = rs.getString("Cardholder_name");
            senddate = rs.getDate("deliverydate");
            pinvscrd = rs.getInt("Pinorcrd");
            nrkarte = rs.getString("Cardno");
            countRows++;


            if (pinvscrd < 1) { /** SMS TEXT per karta ***/

                /** ---------------------------- */
                /** RUN THE HTTP for SMS ERIONI  */
                /** ---------------------------- */

                String myurltext1 = "http://80.78.74.210:8888/?User=***&Password=***"; /** Test */
                
                String myurltext2 = "&Text=";
                String myurltext3 = "&PhoneNumber=";
                String myurltext4 = "&Sender=Fibank&ReceiptRequested=Yes";
                String msgbody = "Pershendetje, Ju njoftojme se karta juaj e re contactless eshte gati ne dege. Mund te kryeni blerje ne POS (dyqane) pa PIN deri ne 4500 LEK pa komision.";
                String httpUrlstringString;
                httpUrlstringString = myurltext1 + myurltext2 + URLEncoder.encode(msgbody) + myurltext3 + phone + myurltext4;

                // Kjo funksion e provovoa per test
                // httpUrlstringString = "https://postman-echo.com/get?foo1=bar1&foo2=bar" + pinvscrd + "'"; //Karte
                // https://github.com/kevinsawicki/http-request
                // Make this update query only iff http request server response is 200. - Not required for the moment since i parsed the title

                /** CHECK IF SMS IS SUBMITED  parse title chekck if it's submitted */
                /** RUN HTTP ONLY IF Phone exist */
                String responsetitle = null;
                String response = null;
                if( phone == null) {
                    responsetitle = "ska telefon";
                    Statement st2 = conn.createStatement();
                    st2.addBatch("UPDATE Test_R7_Datatable SET Test_R7_Datatable.SMS_LOG = \" " + responsetitle + " \" , Test_R7_Datatable.Sent = True WHERE (((Test_R7_Datatable.Cardno)=\""+nrkarte+" \") AND ((Test_R7_Datatable.DeliveryDate)=#6/26/2020#) AND ((Test_R7_Datatable.sent)=False) AND ((Test_R7_Datatable.[card sequence no])<1))");
                    st2.executeBatch(); /** shtoje perpara where ,Test_R7_Datatable.Sent = True */
                    // Debug  System.out.println(" Karta - Phone null - : "+ phone + crdname +  responsetitle);
                    myWriter.write("Derguar SMS: CardName:" + crdname + " Phone:" + phone + " Send date:" + senddate + " Pin/Card:" + pinvscrd + " httpresponse:" + responsetitle + newLine);
                }
                else {
                    response = HttpRequest.get(httpUrlstringString).body();
                    responsetitle = parse_html(response);
                    boolean message_submitted = Pattern.compile(Pattern.quote("Message Submitted"), Pattern.CASE_INSENSITIVE).matcher(responsetitle).find();
                    if (message_submitted == true){

                        Statement st2 = conn.createStatement();
                        st2.addBatch("UPDATE Test_R7_Datatable SET Test_R7_Datatable.SMS_LOG = \" " + responsetitle + " \" , Test_R7_Datatable.Sent = True WHERE (((Test_R7_Datatable.Cardno)=\""+nrkarte+"\") AND ((Test_R7_Datatable.DeliveryDate)=#6/26/2020#) AND ((Test_R7_Datatable.sent)=False) AND ((Test_R7_Datatable.[card sequence no])<1))");
                        st2.executeBatch(); /** shtoje perpara where , Test_R7_Datatable.Sent = True */
                        // Debug      System.out.println(" Karta - Phone not null  - Message Submited - : "+ phone + crdname +  responsetitle);
                        myWriter.write("Derguar SMS: CardName:" + crdname + " Phone:" + phone + " Send date:" + senddate + " Pin/Card:" + pinvscrd + " httpresponse:" + responsetitle + newLine);
                    }
                    else {
                        Statement st2 = conn.createStatement();
                        st2.addBatch("UPDATE Test_R7_Datatable SET Test_R7_Datatable.SMS_LOG = \" HTTP Gabim:" + responsetitle + " \" , Test_R7_Datatable.Sent = True  WHERE (((Test_R7_Datatable.Cardno)=\""+nrkarte+"\") AND ((Test_R7_Datatable.DeliveryDate)=#6/26/2020#) AND ((Test_R7_Datatable.sent)=False) AND ((Test_R7_Datatable.[card sequence no])<1))");
                        st2.executeBatch(); /** shtoje perpara where , Test_R7_Datatable.Sent = True */
                        st2.close();
                        // Debug         System.out.println(" Karta - Phone not null - HTTP GABIM - : "+ phone + crdname +  responsetitle);
                        myWriter.write("Derguar SMS: CardName:" + crdname + " Phone:" + phone + " Send date:" + senddate + " Pin/Card:" + pinvscrd +" httpresponse:" +  responsetitle + newLine);
                    }


                }

                /** Printo te gjithe HTML
                 * MsgBox.infoBox( " REsponse: "+ response, "AUTORUN SMS");
                 * MsgBox.infoBox( " Karta Final: "+ crdname + responsetitle  , "AUTORUN SMS"); */

                // Debug    System.out.println(" Karta - FINAL - OUTside LOOPS  : "+ phone + crdname +  responsetitle);

                //System.out.println(response);
            } else {
                String myurltext1 = "http://80.78.74.210:8888/?User=******&Password=****"; /** Test */
                String myurltext2 = "&Text=";
                String myurltext3 = "&PhoneNumber=";
                String myurltext4 = "&Sender=Fibank&ReceiptRequested=Yes";
                String msgbody = "Pershendetje, Ju njoftojme se PINi juaj eshte gati ne dege. Me karten contactless mund te kryeni blerje ne POS (dyqane) pa PIN deri ne 4500 LEK pa komision.";
                String httpUrlstringString;
                httpUrlstringString = myurltext1 + myurltext2 +URLEncoder.encode(msgbody) + myurltext3 + phone + myurltext4;
                // Debug    System.out.println(httpUrlstringString);

                /** CHECK IF SMS IS SUBMITED  parse title chekck if it's submitted */
                /** RUN HTTP ONLY IF Phone exist */
                String responsetitle = null;
                String response = null;
                if( phone == null) {
                    responsetitle = "ska telefon";
                    Statement st2 = conn.createStatement();
                    st2.addBatch("UPDATE Test_R7_Datatable SET Test_R7_Datatable.SMS_LOG = \" " + responsetitle + " \" , Test_R7_Datatable.Sent = True  WHERE (((Test_R7_Datatable.Cardno)=\"" +nrkarte+ "\") AND ((Test_R7_Datatable.DeliveryDate)=#6/26/2020#) AND ((Test_R7_Datatable.sent)=False) AND ((Test_R7_Datatable.[card sequence no])>0))");
                    st2.executeBatch(); /** shtoje perpara where , Test_R7_Datatable.Sent = True */
                    st2.close();
                    // Debug    System.out.println(" PIN - Phone null  : "+ phone + crdname +  responsetitle);
                    myWriter.write("Derguar SMS: CardName:" + crdname + " Phone:" + phone + " Send date:" + senddate + " Pin/Card:" + pinvscrd +" httpresponse:" +  responsetitle + newLine);

                } else {
                    response = HttpRequest.get(httpUrlstringString).body();
                    responsetitle = parse_html(response);
                    boolean message_submitted = Pattern.compile(Pattern.quote("Message Submitted"), Pattern.CASE_INSENSITIVE).matcher(responsetitle).find();
                    if (message_submitted == true)
                    {
                        Statement st2 = conn.createStatement();
                        st2.addBatch("UPDATE Test_R7_Datatable SET Test_R7_Datatable.SMS_LOG = \" " + responsetitle + " \" , Test_R7_Datatable.Sent = True WHERE (((Test_R7_Datatable.Cardno)=\""+nrkarte+"\") AND ((Test_R7_Datatable.DeliveryDate)=#6/26/2020#) AND ((Test_R7_Datatable.sent)=False) AND ((Test_R7_Datatable.[card sequence no])>0))");
                        st2.executeBatch(); /** shtoje perpara where , Test_R7_Datatable.Sent = True */
                        st2.close();
                        // Debug       System.out.println(" PIN - Phone NOT Null, Message submited  : "+ phone + crdname +  responsetitle);
                        myWriter.write("Derguar SMS: CardName:" + crdname + " Phone:" + phone + " Send date:" + senddate + " Pin/Card:" + pinvscrd +" httpresponse:" +  responsetitle + newLine);
                    }
                    else {
                        Statement st2 = conn.createStatement();
                        st2.addBatch("UPDATE Test_R7_Datatable SET Test_R7_Datatable.SMS_LOG = \" GABIM:" + responsetitle + " \" , Test_R7_Datatable.Sent = True  WHERE (((Test_R7_Datatable.Cardno)=\""+nrkarte+"\") AND ((Test_R7_Datatable.DeliveryDate)=#6/26/2020#) AND ((Test_R7_Datatable.sent)=False) AND ((Test_R7_Datatable.[card sequence no])>0))");
                        st2.executeBatch(); /** shtoje perpara where , Test_R7_Datatable.Sent = True */
                        st2.close();
                        // Debug      System.out.println(" PIN - Phone NOT Null, Message ERROR  : "+ phone + crdname +  responsetitle);
                        myWriter.write("Derguar SMS: CardName:" + crdname + " Phone:" + phone + " Send date:" + senddate + " Pin/Card:" + pinvscrd + " httpresponse:" + responsetitle + newLine);
                    }
                }

                /** Printo te gjithe HTML
                 * MsgBox.infoBox( " Response: "+ response, "AUTORUN SMS"); */
                //  MsgBox.infoBox( " PIN Res: "+ crdname + responsetitle, "AUTORUN SMS");
                //   Debug     System.out.println(" PIN - outside loops: "+ phone + crdname +  responsetitle);


            };
            // Debuging
//            System.out.print(crdname);
//            System.out.print(" ");
//            System.out.print(phone);
//            System.out.print(" ");
//            System.out.print(senddate);
//            System.out.print(" ");
//            System.out.print(pinvscrd);
//            System.out.println(" ");


        }


        myWriter.close();
        String isbosh= null;

        if (countRows==0)
        {
            isbosh = "Nuk ka Rekorde !!!";
            MsgBox.infoBox(isbosh, "GABIM!");
        }
        else {
            MsgBox.infoBox("AJNA!  >>>> " + countRows + " <<< SMS u Ruajten", "AUTORUN SMS");
            // String response = HttpRequest.get("http://www Ajna u derguan x SMS ").body();

        }
    }


}
