import ap.mnemosyne.enums.ConstraintTemporalType;
import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.Point;
import ap.mnemosyne.resources.Task;
import ap.mnemosyne.resources.TaskTimeConstraint;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalTime;
import java.util.ArrayList;

public class TaskTest
{

	@Test
	public void testTask() throws JsonProcessingException, IOException
	{
		ArrayList<Point> plist = new ArrayList<>();
		plist.add(new Point(48.44, -123.37));
		System.out.println(
				new Task(12,"asd@asd.it" , "Nome", new TaskTimeConstraint(LocalTime.of(16,0), ParamsName.time_bed, ConstraintTemporalType.dopo),
						false, false, false, false, plist).toJSON()
		);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(new TaskTimeConstraint(LocalTime.of(16,0), ParamsName.time_bed, ConstraintTemporalType.dopo));
		System.out.println(baos.toString());
		oos.close();
		baos.close();
	}
}