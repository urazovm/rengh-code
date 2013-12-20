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
		a.a(); // k为0
		a.a(); // k 为1
		System.out.println(a.i);
		System.out.println(b.i);
		new A().a(); // 匿名对象1
		new A().a(); // 匿名对象2
		// 两个匿名对象不是一个对象
	}
}