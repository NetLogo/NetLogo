#include "../videoDeviceUtils.h"

#include <mfapi.h>
#include <mfidl.h>

template <typename T> void SafeRelease(T** pointer)
{
    if (*pointer)
    {
        (*pointer)->Release();

        *pointer = nullptr;
    }
}

JNIEXPORT jobjectArray JNICALL Java_org_nlogo_extensions_vid_util_VideoDeviceUtils_getDeviceNames(JNIEnv* env, jobject obj)
{
    jobjectArray empty = env->NewObjectArray(0, env->FindClass("java/lang/String"), env->NewStringUTF(""));

    if (FAILED(MFStartup(MF_VERSION)))
    {
        return empty;
    }

    IMFAttributes* attributes;

    if (FAILED(MFCreateAttributes(&attributes, 1)))
    {
        SafeRelease(&attributes);

        MFShutdown();

        return empty;
    }

    if (FAILED(attributes->SetGUID(MF_DEVSOURCE_ATTRIBUTE_SOURCE_TYPE, MF_DEVSOURCE_ATTRIBUTE_SOURCE_TYPE_VIDCAP_GUID)))
    {
        SafeRelease(&attributes);

        MFShutdown();

        return empty;
    }

    IMFActivate** devices = nullptr;

    UINT32 count = 0;

    if (FAILED(MFEnumDeviceSources(attributes, &devices, &count)) || count == 0)
    {
        SafeRelease(&attributes);

        for (unsigned int i = 0; i < count; i++)
        {
            SafeRelease(devices + i);
        }

        CoTaskMemFree(devices);

        MFShutdown();

        return empty;
    }

    jobjectArray names = env->NewObjectArray(count, env->FindClass("java/lang/String"), env->NewStringUTF(""));

    for (unsigned int i = 0; i < count; i++)
    {
        LPWSTR wname;
        UINT32 length;

        if (FAILED(devices[i]->GetAllocatedString(MF_DEVSOURCE_ATTRIBUTE_FRIENDLY_NAME, &wname, &length)))
        {
            SafeRelease(&attributes);

            for (unsigned int i = 0; i < count; i++)
            {
                SafeRelease(devices + i);
            }

            CoTaskMemFree(devices);

            MFShutdown();

            return empty;
        }

        char* name = (char*)malloc(sizeof(char) * (length + 1));

        const size_t converted = wcstombs(name, wname, length);

        name[converted] = '\0';

        env->SetObjectArrayElement(names, i, env->NewStringUTF(name));

        free(name);
    }

    SafeRelease(&attributes);

    for (unsigned int i = 0; i < count; i++)
    {
        SafeRelease(devices + i);
    }

    CoTaskMemFree(devices);

    MFShutdown();

    return names;
}
