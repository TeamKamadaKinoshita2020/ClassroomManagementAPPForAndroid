package jp.ac.ibaraki.felicacardidlinkapp;

/**
 * Created by 大学用 on 2017/09/05.
 */
import android.util.Log;

import java.io.UnsupportedEncodingException;

import java.util.List;

import jp.ac.ibaraki.felicacardidlinkapp.nfclib.FelicaTag;
import jp.ac.ibaraki.felicacardidlinkapp.nfclib.FelicaTag.ServiceCode;
import jp.ac.ibaraki.felicacardidlinkapp.nfclib.FelicaTag.SystemCode;
import jp.ac.ibaraki.felicacardidlinkapp.nfclib.NfcException;
import jp.ac.ibaraki.felicacardidlinkapp.nfclib.NfcTag;

public class CardInfo
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

    public String cardId;

    public boolean fullInfo = false;
    private NfcTag nfcTag;
    private FelicaTag felicaTag;

    public CardInfo(NfcTag nTag)
    {
        this.setDefault();
        this.nfcTag = nTag;
    }

    public CardInfo(NfcTag nTag, boolean fullInfo)
    {
        this.setDefault();
        this.fullInfo = fullInfo;
        this.nfcTag = nTag;
    }

    private void setDefault()
    {
        this.cardId = "";
    }

    /**
     * 座席カードのID(idm)を取得する
     * @return
     */
    public int getInfo()
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
            Log.d("d","1");

            /*systemCodeList =*////this.felicaTag.getSystemCodeList();



            this.cardId = felicaTag.getIdm().simpleToString();
            Log.d("a",this.cardId);
            return SUCCESSFUL_READING;

            /*for (int i = 0; i < systemCodeList.length && !cardValidity; i++)
            {
                if (systemCodeList[i].simpleToString().equalsIgnoreCase(SYSTEM_CODE))
                {
                    cardValidity = true;
                    sysCodeIndex = i;
                }
                Log.d("d",""+i);
            }
            Log.d("d","2");
            cardValidity = (systemCodeList.length == 2);
            if (!cardValidity)
            {
                return LENGTH_ERROR;
            }
            Log.d("d","3");
            this.felicaTag.polling(systemCodeList[sysCodeIndex]);
            //serviceCodeList = this.felicaTag.getServiceCodeList();


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
            }*/

        }
        catch (Exception e)
        {
            //Idmの表示
            Log.d("idm","idmは" + felicaTag.getIdm().simpleToString());
            return UNKNOWN_ERROR;
        }

        /*this.cardId = felicaTag.getIdm().simpleToString();
        return SUCCESSFUL_READING;*/
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