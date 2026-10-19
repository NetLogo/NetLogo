#include "../videoDeviceUtils.h"

#include <cstring>
#include <fcntl.h>
#include <filesystem>
#include <sys/ioctl.h>
#include <linux/videodev2.h>
#include <string>
#include <unistd.h>
#include <vector>

jobjectArray Java_org_nlogo_extensions_vid_util_VideoDeviceUtils_getDeviceNames(JNIEnv* env, jobject obj)
{
    jobjectArray empty = env->NewObjectArray(0, env->FindClass("java/lang/String"), env->NewStringUTF(""));

    std::vector<char*> names_vector;

    for (const std::filesystem::directory_entry& entry : std::filesystem::directory_iterator({ "/dev" }))
    {
        if (!entry.is_directory() && std::strncmp(entry.path().filename().c_str(), "video", 5) == 0)
        {
            int fd = open(entry.path().c_str(), O_RDONLY);

            if (fd == -1)
            {
                return empty;
            }

            v4l2_capability capability;

            if (ioctl(fd, VIDIOC_QUERYCAP, &capability) != 0)
            {
                close(fd);

                return empty;
            }

            close(fd);

            char* name = (char*)malloc(sizeof(char) * 32);

            strncpy(name, (char*)capability.card, 32);

            names_vector.push_back(name);
        }
    }

    jobjectArray names = (jobjectArray)env->NewObjectArray(names_vector.size(), env->FindClass("java/lang/String"), env->NewStringUTF(""));

    for (unsigned int i = 0; i < names_vector.size(); i++)
    {
        env->SetObjectArrayElement(names, i, env->NewStringUTF(names_vector[i]));

        free(names_vector[i]);
    }

    return names;
}
