package relations_proj;

import java.rmi.server.UID;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Actions {
	
public static List<User> getall() throws ClassNotFoundException{
List<User> result=new ArrayList<User>();
		
try {
	Class.forName("oracle.jdbc.driver.OracleDriver"); 
	Connection con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl","sys as sysdba","11999911");
	Statement stmt=con.createStatement();  
	
	ResultSet rs=stmt.executeQuery("Select u.ID ID, u.FIRSTNAME FIRSTNAME, u.LASTNAME LASTNAME, u.DOB DOB, u.GENDERID GENDERID, utph.USERID USERIDPH, "
	+ "utph.PHONEID PHONEID, ph.ID PHID, ph.TELNUM TELNUM, g.ID GNID, g.TITLE TITLE "
	+ "from Users u left join userphones utph on utph.UserId = u.id "
	+ "left join Phones ph on ph.id = utph.phoneid join Genders g on g.id = u.genderid "
	+ " order by u.id, ph.id");
	
User u = null;
Phone ph = new Phone();
Address ad = null;
String Idcoll = "";
while(rs.next())  {
	if(u == null || u.Id!=rs.getInt("ID")) {
		u= new User();			
		u.Id = rs.getInt(1);
		u.FirstName = rs.getString(2);
		u.LastName = rs.getString(3);
		u.DoB = rs.getDate("DOB");
		u.PhoneNumbers = new ArrayList<Phone>();
		u.AddressNumbers = new ArrayList<Address>();
		result.add(u);
		Idcoll=Idcoll+u.Id+",";
	}
	
ph = new Phone();
ph.ID=rs.getInt("PHID");
ph.phone=rs.getString("telnum");

	if (ph.phone != null) {
		u.PhoneNumbers.add(ph);
		}
	
}
			
stmt.executeBatch();
rs=stmt.executeQuery("Select uad.USERID ID, "
+ "ad.ID ADID, ad.ADDRESS ADDRESS from useraddresses uad"
+ " join addresses ad on ad.id=uad.addressid where uad.userid in ("+Idcoll+"0) order by uad.userid, ad.id");

int uIdx = 0;
while (rs.next()) {
	int id = rs.getInt("ID");
	while (result.get(uIdx).Id != id) {
		uIdx++;
	}
	User cu = result.get(uIdx);
	ad = new Address();
	ad.ID = rs.getInt("ADID");
	ad.Address = rs.getString("Address");
	cu.AddressNumbers.add(ad);	
}


int i=0;

for (User u1: result) {
	System.out.println(u1.Id+"\t"+u1.FirstName+"\t"+u1.LastName+"\t"+u1.DoB+"\t");	
				
		if (u1.PhoneNumbers.size() > 0) {
			System.out.print("Телефоны:");
			for(int i1= 0;i1<u1.PhoneNumbers.size();i1++) {
				System.out.print(u1.PhoneNumbers.get(i1).phone);
				if(i1!=u1.PhoneNumbers.size()-1) {
					System.out.print(", ");
				}
				
			}
		
			System.out.println();
		}
		
		if (u1.AddressNumbers.size() > 0) {
			System.out.print("Адресса:");
				for (Address ad1:u1.AddressNumbers) {
				System.out.print(ad1.Address);
				i++;
						
					if(i<u1.AddressNumbers.size()) {
						System.out.print(", ");
					}
					else {
						i=0;
					}
				}
		
			System.out.println();
		}
			
				
}
			
con.close();
}
	
catch (SQLException e) {
	e.printStackTrace();
}   
		
		return result;
	}

public static boolean Insert_User(User u, Phone p, Address ad ) {
	try {
        DateFormat df = new SimpleDateFormat("dd-MM-YYYY");
Connection con = DriverManager.getConnection(  "jdbc:oracle:thin:@localhost:1521:orcl","sys as sysdba","11999911");
Statement stmt=con.createStatement();
		
String query = "INSERT INTO \"SYS\".\"USERS\" (FIRSTNAME, LASTNAME, DOB, GENDERID) VALUES ('"+
		u.FirstName+"','"+u.LastName+"'," +"To_DATE("+"'"+df.format(u.DoB)+"'" +", 'DD-MM-YYYY '), "+u.GenID+")";
		stmt.executeUpdate(query);
stmt.executeQuery( "declare\r\n"
		+ "exst number(1);\r\n"
		+ "lastid number(18,0);\r\n"
		+ "begin\r\n"
		+ "select case \r\n"
		+ "when not exists(select id from phones where telnum='"+p.phone+"') \r\n"
		+ "then 1\r\n"
		+ "else 0\r\n"
		+ "end  into exst\r\n"
		+ "from dual;\r\n"
		+ "SELECT id into lastid FROM users where rownum=1 ORDER BY id DESC;\r\n"
		+ "if exst = 1 \r\n"
		+ "then  insert into phones (telnum) values('"+p.phone+"');\r\n"
		+ "end if;\r\n"
		+ "insert into userphones (userid, phoneid) values( lastid\r\n"
		+ ",(select id from phones where telnum='"+p.phone+"'));\r\n"
		+ "end;");

stmt.executeQuery("declare\r\n"
		+ "exst number(1);\r\n"
		+ "lastid number(18,0);\r\n"
		+ "begin\r\n"
		+ "select case \r\n"
		+ "when not exists(select id from addresses where address='"+ad.Address+"')    \r\n"
		+ "then 1\r\n"
		+ "else 0\r\n"
		+ "end  into exst\r\n"
		+ "from dual;\r\n"
		+ "SELECT id into lastid FROM users where rownum=1 ORDER BY id DESC;\r\n"
		+ "if exst = 1 \r\n"
		+ "then  insert into addresses (address) values('"+ad.Address+"');\r\n"
		+ "end if;\r\n"
		+ "insert into useraddresses (userid, addressid) values( lastid\r\n"
		+ ",(select id from addresses where address='"+ad.Address+"'));\r\n"
		+ "end;");
		con.close();
return true;
}
	
catch(SQLException e) {
	e.printStackTrace();
return false;
}
}

public static boolean phone_update(User u, Phone p) {
	try {
		Connection con = DriverManager.getConnection(  "jdbc:oracle:thin:@localhost:1521:orcl","sys as sysdba","11999911");
		Statement stmt=con.createStatement();
		stmt.executeQuery("declare\r\n"
				+ "	exst number(1);\r\n"
				+ "	begin\r\n"
				+ "	select case \r\n"
				+ "	when not exists(select id from phones where telnum='"+p.phone+"') \r\n"
				+ "	then 1\r\n"
				+ "	else 0\r\n"
				+ "	end  into exst\r\n"
				+ "	from dual;\r\n"
				+ "	if exst = 1 \r\n"
				+ "	then  insert into phones (telnum) values('"+p.phone+"');\r\n"
				+ "	end if;\r\n"
				+ " update userphones set phoneid = (select id from phones where telnum='"+p.phone+"') where userid = "+u.Id+";\r\n"
				+ "	end;");
		con.close();
	return true;
	}
	catch(SQLException e) {
		return false;
	}
}

public static boolean address_update(User u, Address ad) {
	try {
		Connection con = DriverManager.getConnection(  "jdbc:oracle:thin:@localhost:1521:orcl","sys as sysdba","11999911");
		Statement stmt=con.createStatement();
		stmt.executeQuery("declare\r\n"
				+ "	exst number(1);\r\n"
				+ "	begin\r\n"
				+ "	select case \r\n"
				+ "	when not exists(select id from addresses where address='"+ad.Address+"') \r\n"
				+ "	then 1\r\n"
				+ "	else 0\r\n"
				+ "	end  into exst\r\n"
				+ "	from dual;\r\n"
				+ "	if exst = 1 \r\n"
				+ "	then  insert into addresses (address) values('"+ad.Address+"');\r\n"
				+ "	end if;\r\n"
				+ " update useraddresses set address.id = (select id from addresses where address='"+ad.Address+"') where userid = "+u.Id+";\r\n"
				+ "	end;");
		con.close();
	return true;
	}
	catch(SQLException e) {
		return false;
	}
}

public static boolean Delete(User u) {
	try {
Connection con = DriverManager.getConnection(  "jdbc:oracle:thin:@localhost:1521:orcl","sys as sysdba","11999911");
Statement stmt=con.createStatement();

String query = "  delete\r\n"
		+ "from\r\n"
		+ "    useraddresses\r\n"
		+ "where\r\n"
		+ "    userid="+u.Id+";\r\n"
		+ "\r\n"
		+ "delete\r\n"
		+ "from\r\n"
		+ "    userphones\r\n"
		+ "where\r\n"
		+ "    userid = "+u.Id+";\r\n"
		+ "\r\n"
		+ "    delete\r\n"
		+ "from    \r\n"
		+ "    users\r\n"
		+ "where\r\n"
		+ "    id="+u.Id+";\r\n"
		+ "COMMIT WORK; ";

	stmt.executeUpdate(query);
	con.close();
return true;
	}
	
catch(SQLException e) {
		e.printStackTrace();
return false;
	}
}

public static List<User> GetByID(int id) {
	try {
Connection con = DriverManager.getConnection(  "jdbc:oracle:thin:@localhost:1521:orcl","sys as sysdba","11999911");
Statement stmt=con.createStatement();
User u = null;
Phone ph = new Phone();
Address ad = null;
List<User> result=new ArrayList<User>();

ResultSet rs=stmt.executeQuery("Select u.ID ID, u.FIRSTNAME FIRSTNAME, u.LASTNAME LASTNAME, u.DOB DOB, u.GENDERID GENDERID, utph.USERID USERIDPH, "
		+ "utph.PHONEID PHONEID, ph.ID PHID, ph.TELNUM TELNUM, g.ID GNID, g.TITLE TITLE "
		+ "from Users u left join userphones utph on utph.UserId = u.id "
		+ "left join Phones ph on ph.id = utph.phoneid join Genders g on g.id = u.genderid where u.ID = "+id
		+ " order by u.id, ph.id");
rs.next();

while(u == null || rs.next())  {
	u = new User();
	if(u.Id!=rs.getInt("ID")) {
u.Id = rs.getInt(1);
u.FirstName = rs.getString(2);
u.LastName = rs.getString(3);
u.DoB = rs.getDate("DOB");
u.PhoneNumbers = new ArrayList<Phone>();
u.AddressNumbers = new ArrayList<Address>();
result.add(u);
}
	ph = new Phone();
ph.ID=rs.getInt("PHID");
ph.phone=rs.getString("telnum");

	if (ph.phone != null) {
		u.PhoneNumbers.add(ph);
		}
}	

stmt.executeBatch();
rs=stmt.executeQuery("Select uad.USERID ID, "
+ "ad.ID ADID, ad.ADDRESS ADDRESS from useraddresses uad"
+ " join addresses ad on ad.id=uad.addressid where uad.userid ="+u.Id+" order by uad.userid, ad.id");

while (rs.next()) {
	ad = new Address();
	ad.ID = rs.getInt("ADID");
	ad.Address = rs.getString("Address");
	u.AddressNumbers.add(ad);	
}
con.close();
int i=0;


	System.out.println(u.Id+"\t"+u.FirstName+"\t"+u.LastName+"\t"+u.DoB+"\t");	
				
		if (u.PhoneNumbers.size() > 0) {
			System.out.print("Телефоны:");
			for(int i1= 0;i1<u.PhoneNumbers.size();i1++) {
				System.out.print(u.PhoneNumbers.get(i1).phone);
				if(i1!=u.PhoneNumbers.size()-1) {
					System.out.print(", ");
				}
				
			}
		
			System.out.println();
		}
		
		if (u.AddressNumbers.size() > 0) {
			System.out.print("Адресса:");
				for (Address ad1:u.AddressNumbers) {
				System.out.print(ad.Address);
				i++;
						
					if(i<u.AddressNumbers.size()) {
						System.out.print(", ");
					}
					else {
						i=0;
					}
				}
		
			System.out.println();
		}
			

return result;
	}
	
catch(SQLException e) {
		e.printStackTrace();
return null;
	}
}
}
