class Person
{
	static int i;
}

class Test
{
	public static void main(String args[])
	{
		// 静态成员变量调用方法除了平常的方法，还可以这样使用：
		Person.i = 10; // 可以直接使用类来调用；
	}
}