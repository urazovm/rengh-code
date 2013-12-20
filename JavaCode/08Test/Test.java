public class Test
{
	public static void main(String args[])
	{
		int i = 5;
		boolean e0 = i > 6 & i++ > 7;
		System.out.println(i);
		
		int j = 5;
		boolean e1 = j > 6 && j++ > 7;
		System.out.println(j);
	}
}