package jp.ac.ibaraki.felicacardidlinkapp;

import android.util.Log;

import java.io.UnsupportedEncodingException;

import java.util.List;

import jp.ac.ibaraki.felicacardidlinkapp.nfclib.FelicaTag;
import jp.ac.ibaraki.felicacardidlinkapp.nfclib.FelicaTag.ServiceCode;
import jp.ac.ibaraki.felicacardidlinkapp.nfclib.FelicaTag.SystemCode;
import jp.ac.ibaraki.felicacardidlinkapp.nfclib.NfcException;
import jp.ac.ibaraki.felicacardidlinkapp.nfclib.NfcTag;

public class StudentInfo
{
	public static String SYSTEM_CODE = "81F8";
	public static String SERVICE_CODE = "100B";
	public static boolean FULL_INFO = true;
	public static boolean PARTIAL_INFO = false;
	public static final int UNKNOWN_ERROR = -1;
	public static final int SUCCESSFUL_READING = 0;
	public static final int NON_FELICA = 1;
	public static final int LENGTH_ERROR = 2;
	public static final int AUTH_REQUIRED = 3;
	public static final int UNSUPPORTED_ENCODING = 4;

	public String name;
	public String kana;
	public String studentNumber;
	public String birthDate;
	public String issueDate;
	public String expireDate;

	public boolean fullInfo = false;
	private NfcTag nfcTag;
	private FelicaTag felicaTag;

	public StudentInfo(NfcTag nTag)
	{
		this.setDefault();
		this.nfcTag = nTag;
	}

	public StudentInfo(NfcTag nTag, boolean fullInfo)
	{
		this.setDefault();
		this.fullInfo = fullInfo;
		this.nfcTag = nTag;
	}

	private void setDefault()
	{
		this.name = "";
		this.kana = "";
		this.studentNumber = "";
		this.birthDate = "";
		this.issueDate = "";
		this.expireDate = "";
	}

	public int getInfo()//学生証の中身（学籍番号、名前、フリガナ）等を読みだしている
	{
		if (this.nfcTag == null || !this.nfcTag.getType().equals(NfcTag.TYPE_FELICA))
		{
			return NON_FELICA;
		}
		this.felicaTag = (FelicaTag) this.nfcTag;
		SystemCode[] systemCodeList = null;
		List<ServiceCode> serviceCodeList = null;
		ServiceCode servCode = null;
		boolean cardValidity = false;
		int sysCodeIndex = 0;
		byte[] blockData = null;
		String tmpStr = "";

		try
		{
			systemCodeList = this.felicaTag.getSystemCodeList();
			for (int i = 0; i < systemCodeList.length && !cardValidity; i++)
			{
				if (systemCodeList[i].simpleToString().equalsIgnoreCase(SYSTEM_CODE))
				{
					cardValidity = true;
					sysCodeIndex = i;
				}
			}
			cardValidity = (systemCodeList.length == 2);
			if (!cardValidity)
			{
				return LENGTH_ERROR;
			}
			this.felicaTag.polling(systemCodeList[sysCodeIndex]);
			serviceCodeList = this.felicaTag.getServiceCodeList();

			cardValidity = false;
			for (ServiceCode sc : serviceCodeList)
			{
				if (sc.simpleToString().equalsIgnoreCase(SERVICE_CODE))
				{
					cardValidity = true;
					servCode = sc;
					break;
				}
			}
			if (!cardValidity || servCode.encryptNeeded())
			{
				return AUTH_REQUIRED;
			}

			//Idmの表示
			//Log.d("idm","idmは" + felicaTag.getIdm().toString());

			//学籍番号
			blockData = this.felicaTag.readWithoutEncryption(servCode, 0);
			this.studentNumber = new String(blockData).substring(0, 8);
			//氏名
			for (int i = 4; i <= 6; i++)
			{
				blockData = this.felicaTag.readWithoutEncryption(servCode, i);
				tmpStr += new String(blockData, "SJIS").trim();
				if (this.dataEnds(blockData))
					break;
			}
			this.name = tmpStr;
			if (!this.fullInfo)
				return SUCCESSFUL_READING;
			//フリガナ
			for (int i = 7; i <= 9; i++)
			{
				blockData = this.felicaTag.readWithoutEncryption(servCode, i);
				this.kana += new String(blockData, "SJIS").trim();
				if (this.dataEnds(blockData))
					break;
			}
			//生年月日、発行日、有効期限
			tmpStr = "";
			for (int i = 10; i <= 11; i++)
			{
				blockData = this.felicaTag.readWithoutEncryption(servCode, i);
				tmpStr += new String(blockData);
			}
			this.birthDate = tmpStr.substring(0, 10);
			this.issueDate = tmpStr.substring(10, 20);
			this.expireDate = tmpStr.substring(20, 30);
		}
		catch (NfcException e)
		{
			//Idmの表示
			Log.d("idm","idmは" + felicaTag.getIdm().simpleToString());

			return UNKNOWN_ERROR;
		}
		catch (UnsupportedEncodingException e)
		{
			//Idmの表示
			Log.d("idm","idmは" + felicaTag.getIdm().simpleToString());

			return UNSUPPORTED_ENCODING;
		}

		return SUCCESSFUL_READING;
	}

	private boolean dataEnds(byte[] blockData)
	{
		byte endPos = blockData[blockData.length - 1];
		if (endPos == 32 || endPos == 0)
			return true;
		else
			return false;
	}
}
