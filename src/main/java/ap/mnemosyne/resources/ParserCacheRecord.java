package ap.mnemosyne.resources;

import ap.mnemosyne.parser.resources.TextualTask;

import java.util.Objects;

public class ParserCacheRecord
{
	private String task;
	private String version;
	private TextualTask result;

	public ParserCacheRecord(String task, String version, TextualTask result)
	{
		this.task = task;
		this.version = version;
		this.result = result;
	}

	public String getTask()
	{
		return task;
	}

	public String getVersion()
	{
		return version;
	}

	public TextualTask getResult()
	{
		return result;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ParserCacheRecord that = (ParserCacheRecord) o;
		return version == that.version &&
				Objects.equals(task, that.task) &&
				Objects.equals(result, that.result);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(task, version, result);
	}

	@Override
	public String toString()
	{
		return "ParserCacheRecord{" +
				"task='" + task + '\'' +
				", version=" + version +
				", result=" + result +
				'}';
	}

}
