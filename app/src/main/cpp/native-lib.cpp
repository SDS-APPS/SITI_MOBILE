//#define _LINUX
//#include "appmsg.h"
//#include <driver/system/piasetup.h>
#include <android/log.h>
#include <sys/stat.h>

#if defined(_WIN32)
#include "stdafx.h"
#include "resource.h"
#include <winsock.h>
HWND g_hwnd;

#elif defined(_LINUX)

#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/socket.h>
#include <time.h>
#include <signal.h>
//#include <linux/tcp.h>
#include <netinet/tcp.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#endif

#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>

#define  LOG_TAG    "testjni"
#define  ALOG(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

#include <jni.h>
#include <string>

#include <cstring>
std::string ConvertJString(JNIEnv* env, jstring str)
{
   if ( !str ) std::string();

   const jsize len = env->GetStringUTFLength(str);
   const char* strChars = env->GetStringUTFChars(str, (jboolean *)0);

   std::string Result(strChars, len);

   env->ReleaseStringUTFChars(str, strChars);

   return Result;
}

uint32_t getDecimalValueOfIPV4_String(const char ipAddress[])
{
	uint8_t ipbytes[4] = {};
	int i = 0;
	int8_t j = 3;
	while (ipAddress + i && i < strlen(ipAddress))
	{
		char digit = ipAddress[i];
		if (isdigit(digit) == 0 && digit != '.') {
			return 0;
		}
		j = digit == '.' ? j - 1 : j;
		ipbytes[j] = ipbytes[j] * 10 + atoi(&digit);

		i++;
	}

	uint32_t a = ipbytes[0];
	uint32_t b = (uint32_t)ipbytes[1] << 8;
	uint32_t c = (uint32_t)ipbytes[2] << 16;
	uint32_t d = (uint32_t)ipbytes[3] << 24;
	return a + b + c + d;
}

void getkey(char ipAddress[], int port,  char key[])
{
	char* chkey = key;
	int i;


	time_t time_raw_format;
	struct tm* ptr_time;
	
	time(&time_raw_format);

	ptr_time = localtime(&time_raw_format);
	int mon = ptr_time->tm_mon;
	int daynum = ptr_time->tm_yday;
    
	int hournum = ptr_time->tm_hour;
    int minnum = ptr_time->tm_min;

	unsigned int ipdecimal = getDecimalValueOfIPV4_String(ipAddress);
	for( i = 0; i<16; i++)
	    chkey[i] = 'a';
	chkey[i] = '\0';

	chkey[0] = ((ipdecimal >>  0) & 0x0ff % 26) + 97;
	chkey[1] = ((ipdecimal >>  8) & 0x0ff % 26) + 97;
	chkey[2] = ((ipdecimal >> 16) & 0x0ff % 26) + 97;
	chkey[3] = ((ipdecimal >> 24) & 0x0ff % 26) + 97;

    chkey[4] = ((port >> 24) & 0x0ff % 26) + 97;
	chkey[5] = ((port >> 16) & 0x0ff % 26) + 97;
	chkey[6] = ((port >>  8) & 0x0ff % 26) + 97;
	chkey[7] = ((port >>  0) & 0x0ff % 26) + 97;

	chkey[8] = ((daynum >> 8) & 0x0ff % 26) + 97;
	chkey[9] = ((daynum >> 0) & 0x0ff % 26) + 97;

	//.chkey[10] = ((hournum >> 0) & 0x0ff );
	//.chkey[11] = ((int)(minnum / 10));
	chkey[10] = ((((int)(minnum / 10)) >> 0) & 0x0ff % 26) + 97;
	return ;

}


int getchanged()
{
	static int premin = 0;
	time_t time_raw_format;
	struct tm* ptr_time;
	
	time(&time_raw_format);

	ptr_time = localtime(&time_raw_format);
	
	int minnum = ptr_time->tm_min;
	int itenmin =  (int) (minnum/10);
	
	if(premin == itenmin){
		
		
		return 0;
	}
	else{
		printf("#################  %d %d\n", premin, itenmin);
		premin = itenmin;
		
		return 1;
	}
	
}

void myMethod() {
    ALOG("This message comes from C at line %d.", __LINE__);
}

extern void *aes_decrypt_init(unsigned char *key, int len);

extern int aes_128_cbc_decrypt(void *ctx, unsigned char *cbc, unsigned char *data, int data_len);


extern void aes_decrypt_deinit(void *ctx);


//AES 128
void *ctx;
unsigned char g_sziv[16] = {0xde, 0xed, 0x2a, 0x88, 0xe7, 0x3d, 0xcc, 0xaa, 0x30, 0xa9, 0xe6, 0xe2,
                            0x96, 0xf6, 0x2b, 0xe2};
unsigned char g_szkey[16] = {0x01, 0x02, 0x03, 0x04, 0x01, 0x02, 0x03, 0x04, 0x01, 0x02, 0x03, 0x04,
                             0x01, 0x02, 0x03, 0x04};
unsigned char g_szkey1[16] = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80};//"ABCDEFGHIJKLMNOP";

unsigned char g_rekey[16];
char* strIP; int iPort;
char tempch[16];

void AES_128_Decrypt_String(char *pOutStr, char *pIntStr, int nLen) {
	/*
    unsigned char *p;

    p = (unsigned char *) pOutStr;
	
    aes_128_cbc_decrypt(ctx, g_sziv, p, nLen);
*/
    unsigned char *p;
    unsigned char gcbc[16];
	memcpy(gcbc,g_sziv, 16);
    p = (unsigned char *) pOutStr;
	
    aes_128_cbc_decrypt(ctx, gcbc, p, nLen);
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_digitalview_sdsiptv_Activity_LoginActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jint JNICALL // change this to our package
Java_com_digitalview_sdsiptv_Activity_Login_JNIAESDecryptStart128(
        JNIEnv *env,
        jobject /* this */, jstring nLen, jbyteArray szEncStr) {
	jboolean isCopy;
    const char *pValue;	
	int mLen = 0;
	char *g_pInput;
	
	pValue = (char *) env->GetStringUTFChars(nLen, &isCopy);
    mLen = atoi(pValue);
	jbyte *pValue1 = env->GetByteArrayElements(szEncStr, &isCopy);
    //.g_pInput = (char *) malloc(mLen);
	memcpy(g_rekey, pValue1, mLen);
	
	
    //.ctx = aes_decrypt_init(g_rekey, 16);
    ctx = aes_decrypt_init(g_szkey1, 16);
    return 1;
}
extern "C" JNIEXPORT jint JNICALL
Java_com_digitalview_sdsiptv_Activity_Login_JNIAESDecryptEnd128(
        JNIEnv *env,
        jobject /* this */) {

    aes_decrypt_deinit(ctx);
    return 1;
}
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_digitalview_sdsiptv_Activity_Login_JNIAESDecrypt(
        JNIEnv *env,
        jobject /* this */, jstring nLen, jbyteArray szEncStr) {
    char *g_pInput;
    jboolean isCopy;
    const char *pValue;
    unsigned char *punValue;
    jstring strresult;
    jbyteArray retbyte;
    int mLen = 0;
    int nRet;

    pValue = (char *) env->GetStringUTFChars(nLen, &isCopy);
    mLen = atoi(pValue);
    jbyte *pValue1 = env->GetByteArrayElements(szEncStr, &isCopy);
    g_pInput = (char *) malloc(mLen);
    memcpy(g_pInput, pValue1, mLen);
    AES_128_Decrypt_String(g_pInput, g_pInput, mLen);
    jbyteArray result = env->NewByteArray(mLen);
    memcpy(pValue1, g_pInput, mLen);
    env->SetByteArrayRegion(result, 0, mLen, pValue1);
    if (g_pInput) free(g_pInput);
    return result;

}

extern "C" JNIEXPORT jstring JNICALL
Java_com_digitalview_sdsiptv_Utils_MulticastWrapper_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    //.return env->NewStringUTF(hello.c_str());
	return env->NewStringUTF(tempch);//tempch
}



extern "C" JNIEXPORT jint JNICALL // change this to our package 
Java_com_digitalview_sdsiptv_Utils_MulticastWrapper_JNIAESDecryptStart128(
        JNIEnv *env,
        jobject /* this */, jstring sIP, jstring sPort) {
	jboolean isCopy;
    const char *pValue;	
	
	int ipstrlength;
	char *g_pInput;
	
	pValue = (char *) env->GetStringUTFChars(sPort, &isCopy);
    iPort = atoi(pValue);
	//.jbyte *pValue1 = env->GetByteArrayElements(szEncStr, &isCopy);
    //.g_pInput = (char *) malloc(mLen);
	//.memset(ipstr, '\0', 32);
	//.memcpy(ipstr, pValue1, ipstrlength);
	
	
	
//.    ctx = aes_decrypt_init(g_rekey, 16);


    ctx = NULL;
	std::string bmpath = ConvertJString( env, sIP );
	const int length = bmpath.length();
	strIP = new char[length + 1];
	strcpy(strIP, bmpath.c_str());

	getkey(strIP, iPort, tempch);
	memcpy(g_szkey1, tempch, 16);
	//.delete strIP;
	
	if(ctx){
		aes_decrypt_deinit(ctx);
		ctx = NULL;
		
	}
	ctx = aes_decrypt_init(g_szkey1, 16);
	
	//..0613
	//jstring str = env->NewStringUTF(tempch);
	//return str;
	//.
	
    return 1;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_digitalview_sdsiptv_Utils_MulticastWrapper_JNIAESDecryptEnd128(
        JNIEnv *env,
        jobject /* this */) {

    aes_decrypt_deinit(ctx);
    return 1;
}
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_digitalview_sdsiptv_Utils_MulticastWrapper_JNIAESDecrypt(
        JNIEnv *env,
        jobject /* this */, jstring nLen, jbyteArray szEncStr) {
    char *g_pInput;
    jboolean isCopy;
    const char *pValue;
    unsigned char *punValue;
    jstring strresult;
    jbyteArray retbyte;
    int mLen = 0;
    int nRet;
	
	if(getchanged()){
		if(ctx){
			getkey(strIP, iPort, tempch);
	        memcpy(g_szkey1, tempch, 16);
		    aes_decrypt_deinit(ctx);
		    ctx = NULL;
			printf("key updated: %s \n",tempch);
	    }
	    ctx = aes_decrypt_init(g_szkey1, 16);
	}

    pValue = (char *) env->GetStringUTFChars(nLen, &isCopy);
    mLen = atoi(pValue);
    jbyte *pValue1 = env->GetByteArrayElements(szEncStr, &isCopy);
    g_pInput = (char *) malloc(mLen);
    memcpy(g_pInput, pValue1, mLen);
    AES_128_Decrypt_String(g_pInput, g_pInput, mLen);
    jbyteArray result = env->NewByteArray(mLen);
    memcpy(pValue1, g_pInput, mLen);
    env->SetByteArrayRegion(result, 0, mLen, pValue1);
    if (g_pInput) free(g_pInput);
    return result;

}