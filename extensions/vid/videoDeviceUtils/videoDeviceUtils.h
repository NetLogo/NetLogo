#include <jni.h>

extern "C"
{
    JNIEXPORT jobjectArray JNICALL Java_org_nlogo_extensions_vid_util_VideoDeviceUtils_getDeviceNames(JNIEnv* env, jobject obj);
}
