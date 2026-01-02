#include <jni.h>
#include "com_vegatrader_market_journal_NativeJournal.h"
#include "JournalWriter.h"
#include "ReplayEngine.h"

// Helper to cast jlong to pointer
template<typename T>
T* getHandle(jlong handle) {
    return reinterpret_cast<T*>(handle);
}

// ABI Version Check
JNIEXPORT jint JNICALL Java_com_vegatrader_market_journal_NativeJournal_nativeAbiVersion
(JNIEnv* env, jclass cls) {
    return 0xDEAD1701;  // Must match Java side check
}

// Writer Init
JNIEXPORT jlong JNICALL Java_com_vegatrader_market_journal_NativeJournal_initWriter
  (JNIEnv *env, jclass cls, jstring filepath, jlong capacity) {
    const char *path = env->GetStringUTFChars(filepath, 0);
    JournalWriter* writer = new JournalWriter(std::string(path), (size_t)capacity);
    env->ReleaseStringUTFChars(filepath, path);
    return reinterpret_cast<jlong>(writer);
}

// Writer Append
JNIEXPORT void JNICALL Java_com_vegatrader_market_journal_NativeJournal_append
  (JNIEnv *env, jclass cls, jlong writerPtr, jint instId, jlong exTs, jlong rxTs, jint type, jbyteArray payload) {
    
    JournalWriter* writer = getHandle<JournalWriter>(writerPtr);
    
    // Construct Header
    EventHeader header;
    header.sequence = 0; // Sequence handled by writer or atomic counter? For now 0.
    header.instrument_id = (uint32_t)instId;
    header.exchange_ts_ns = (uint64_t)exTs;
    header.receive_ts_ns = (uint64_t)rxTs;
    header.event_type = (uint16_t)type;
    
    jsize len = env->GetArrayLength(payload);
    header.payload_size = (uint16_t)len;
    
    // Copy payload
    jbyte* body = env->GetByteArrayElements(payload, NULL);
    
    writer->append(header, body);
    
    env->ReleaseByteArrayElements(payload, body, 0);
}

// Writer Close
JNIEXPORT void JNICALL Java_com_vegatrader_market_journal_NativeJournal_closeWriter
  (JNIEnv *env, jclass cls, jlong writerPtr) {
    JournalWriter* writer = getHandle<JournalWriter>(writerPtr);
    delete writer;
}

// Replay Init
JNIEXPORT jlong JNICALL Java_com_vegatrader_market_journal_NativeJournal_initReplay
  (JNIEnv *env, jclass cls, jstring filepath) {
    const char *path = env->GetStringUTFChars(filepath, 0);
    ReplayEngine* engine = new ReplayEngine(std::string(path));
    env->ReleaseStringUTFChars(filepath, path);
    return reinterpret_cast<jlong>(engine);
}

// Replay Seek
JNIEXPORT jlong JNICALL Java_com_vegatrader_market_journal_NativeJournal_seek
  (JNIEnv *env, jclass cls, jlong replayPtr, jlong timestamp) {
    ReplayEngine* engine = getHandle<ReplayEngine>(replayPtr);
    return (jlong)engine->seek((uint64_t)timestamp);
}

// Replay Close
JNIEXPORT void JNICALL Java_com_vegatrader_market_journal_NativeJournal_closeReplay
  (JNIEnv *env, jclass cls, jlong replayPtr) {
    ReplayEngine* engine = getHandle<ReplayEngine>(replayPtr);
    delete engine;
}
