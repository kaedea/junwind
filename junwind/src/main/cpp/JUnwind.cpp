#include <jni.h>
#include <unistd.h>
#include <cinttypes>
#include <sys/time.h>
#include <pthread.h>
#include <android/log.h>
#include <cstring>
#include <array>
#include <string>
#include <sstream>
#include <dlfcn.h>
#include <link.h>
#include <sys/mman.h>
#include <sys/auxv.h>
#include <map>
#include <android/trace.h>
#include <dirent.h>

#define EXPORT extern "C"  __attribute__ ((visibility ("default")))

#define LOGD(TAG, FMT, args...) //__android_log_print(ANDROID_LOG_DEBUG, TAG, FMT, ##args)
#define TAG "JUnwindNative"

#if defined(__GLIBC__)
// In order to run the backtrace_tests on the host, we can't use
// the internal real time signals used by GLIBC. To avoid this,
// use SIGRTMIN for the signal to dump the stack.
#define THREAD_SIGNAL SIGRTMIN
#else
#define THREAD_SIGNAL (__SIGRTMIN+1)
static const char *const JNI_CLASS_REF = "com/kaedea/junwind/JUnwind";
static const char *const JNI_GET_THROWABLE_METHOD_NAME = "currJavaThrowable";
static const char *const JNI_GET_THROWABLE_METHOD_SIGN = "()Ljava/lang/String;";
#endif


pthread_cond_t sCond = PTHREAD_COND_INITIALIZER_MONOTONIC_NP;
pthread_mutex_t sMutex = PTHREAD_MUTEX_INITIALIZER;
JavaVM *sJVM = nullptr;
jclass sClazzRef = nullptr;
bool sWaiting = false;

static void getJavaThrowableStackTrace(JNIEnv *env, jclass clazz) {
    LOGD(TAG, "get java stack of curr thread");
    if (!env->ExceptionCheck()) {
        jmethodID mid = env->GetStaticMethodID(
                clazz,
                JNI_GET_THROWABLE_METHOD_NAME,
                JNI_GET_THROWABLE_METHOD_SIGN);
        if (mid != nullptr) {
            env->CallStaticObjectMethod(clazz, mid);
        }
    }
}

static void signalHandler(int sigNum) {
    LOGD(TAG, "on handle signal, sigNum = " + sigNum);
    // usleep(5000 * 1000);

    if (sJVM != nullptr) {
        JNIEnv* env = nullptr;
        bool attached = false;
        if (sJVM->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) {
            if (sJVM->AttachCurrentThread(&env, nullptr) == JNI_OK) {
                attached = true;
            }
        }
        if (env != nullptr) {
            jclass jniClass = sClazzRef;
            if (jniClass == nullptr) {
                jniClass = env->FindClass(JNI_CLASS_REF);
            }
            if (jniClass != nullptr) {
                getJavaThrowableStackTrace(env, jniClass);
                if (attached) {
                    sJVM->DetachCurrentThread();
                }
            }
        } else {
            LOGD(TAG, "fail: jni env is null");
        }
    }

    if (sWaiting) {
        pthread_mutex_lock(&sMutex);
        pthread_cond_signal(&sCond);
        pthread_mutex_unlock(&sMutex);
    }
}

EXPORT jstring
Java_com_kaedea_junwind_JUnwind_nativeJUnwind(JNIEnv *env, jclass clazz, jint tid) {
    LOGD(TAG, "#nativeJUnwind, tid = " + tid);
    if (gettid() == tid) {
        LOGD(TAG, "jUnwind curr thread");
        return nullptr;
    }

    if (sJVM == nullptr) {
        env->GetJavaVM(&sJVM);
        if (sJVM == nullptr) {
            LOGD(TAG, "fail: jvm is null");
            return nullptr;
        }
        sClazzRef = (jclass) env->NewGlobalRef(clazz);
    }

    struct sigaction newSigAct, oldSigAct;
    newSigAct.sa_flags = SA_RESTART;
    newSigAct.sa_handler = signalHandler;
    if (sigaction(THREAD_SIGNAL, &newSigAct, &oldSigAct) != 0) {
        LOGD(TAG, "fail: regs signal handler fail");
        return nullptr;
    }

    if (tgkill(getpid(), tid, THREAD_SIGNAL) != 0) {
        sigaction(THREAD_SIGNAL, &oldSigAct, nullptr);
        LOGD(TAG, "fail: send signal handler fail");
        return nullptr;
    }

    timespec ts{0, 0};
    clock_gettime(CLOCK_MONOTONIC, &ts);
    ts.tv_sec += 1;
    // ts.tv_sec += 100;
    pthread_mutex_lock(&sMutex);
    sWaiting = true;
    pthread_cond_timedwait(&sCond, &sMutex, &ts);
    sWaiting = false;
    pthread_mutex_unlock(&sMutex);

    sigaction(THREAD_SIGNAL, &oldSigAct, nullptr);
    return nullptr;
}