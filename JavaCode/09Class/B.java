class A
{
	int i;
	int k = 0;
	void a()
	{
		System.out.println(k++);;
	}
}

public class B // class B
{
	public static void main(String args[])
	{
		A a = new A();
		A b = new A();
		b.i = 2;
		a.i = 1;
		a.a(); // kΪ0
		a.a(); // k Ϊ1
		System.out.println(a.i);
		System.out.println(b.i);
		new A().a(); // ��������1
		new A().a(); // ��������2
		// ��������������һ������
	}
}