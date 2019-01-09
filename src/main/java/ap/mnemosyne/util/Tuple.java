package ap.mnemosyne.util;

import java.util.Objects;

public class Tuple<K, V>
{
	K left;
	V right;

	public Tuple(K left, V right)
	{
		this.left = left;
		this.right = right;
	}

	public K getLeft()
	{
		return left;
	}

	public void setLeft(K left)
	{
		this.left = left;
	}

	public V getRight()
	{
		return right;
	}

	public void setRight(V right)
	{
		this.right = right;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Tuple<?, ?> tuple = (Tuple<?, ?>) o;
		return Objects.equals(left, tuple.left) &&
				Objects.equals(right, tuple.right);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(left, right);
	}

	@Override
	public String toString()
	{
		return "Tuple{" +
				"left=" + left +
				", right=" + right +
				'}';
	}

}
