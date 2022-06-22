package relations_proj;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
	
public static void main(String[] args) throws ClassNotFoundException {
	
	User u = new User();
	Phone p = new Phone();
	Gender g = new Gender();
	Address ad = new Address();
	u.Id= 1;
	u.FirstName="oybek";
	u.LastName= "mamajonov";
	u.GenID=1;
	u.DoB=new Date("05/06/1997");
	DateFormat df = new SimpleDateFormat("dd-MM-YYYY");
	p.phone="999";

	Actions.GetByID(u.Id);

}
}
