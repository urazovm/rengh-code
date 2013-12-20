class Test
{
	public static void main(String args[])
	{
		int flag = 1;
		
		if(flag == 0)
		{
			HpPrinter hpPrinter = new HpPrinter();
			hpPrinter.open();
			hpPrinter.print("abcd");
			hpPrinter.close();
		}
		else if(flag != 0)
		{
			MsPrinter msPrinter = new MsPrinter();
			msPrinter.open();
			msPrinter.print("1234");
			msPrinter.close();
		}
	}
}