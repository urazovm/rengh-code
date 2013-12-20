class HpPrinter extends Printer
{
	void close()
	{
		this.clean();
		super.close();
	}
	
	void clean()
	{
		System.out.println("«Â¿Ì");
	}
}