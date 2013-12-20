class Person
{
	String name;
	int age;
	String address;
	
	Person()
	{
		System.out.println("无参数构造函数");
	}
	
	Person(String name, int age)
	{
		this(); // 必须是第一条语句,每个构造函数最多调用一次其他参数少的构造函数
		this.name = name;
		this.age = age;
		System.out.println("两个参数的构造函数");
	}
	
	Person(String name, int age, String address)
	{
		this(name, age); // 必须是第一条语句,每个构造函数最多调用一次其他参数少的构造函数
		this.address = address;
		System.out.println("三个参数的构造函数");
	}
}

class Info
{
	public static void main(String args[])
	{
		Person p1 = new Person("ZhangSan", 29, "BeiJing");
	}
}