class Person
{
	String name;
	int age;
	String address;
	
	Person()
	{
		System.out.println("�޲������캯��");
	}
	
	Person(String name, int age)
	{
		this(); // �����ǵ�һ�����,ÿ�����캯��������һ�����������ٵĹ��캯��
		this.name = name;
		this.age = age;
		System.out.println("���������Ĺ��캯��");
	}
	
	Person(String name, int age, String address)
	{
		this(name, age); // �����ǵ�һ�����,ÿ�����캯��������һ�����������ٵĹ��캯��
		this.address = address;
		System.out.println("���������Ĺ��캯��");
	}
}

class Info
{
	public static void main(String args[])
	{
		Person p1 = new Person("ZhangSan", 29, "BeiJing");
	}
}