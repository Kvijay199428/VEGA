#ifndef REPLAY_ENGINE_H
#define REPLAY_ENGINE_H

#include <functional>
#include <algorithm>
#include "JournalWriter.h"

class ReplayEngine {
public:
    ReplayEngine(const std::string& filepath) {
#ifdef _WIN32
        m_hFile = CreateFileA(filepath.c_str(), GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE, 
                             NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
        if (m_hFile == INVALID_HANDLE_VALUE) throw std::runtime_error("Open failed");
        
        m_size = GetFileSize(m_hFile, NULL);
        m_hMap = CreateFileMappingA(m_hFile, NULL, PAGE_READONLY, 0, 0, NULL);
        if (!m_hMap) { CloseHandle(m_hFile); throw std::runtime_error("Map failed"); }
        
        m_ptr = (char*)MapViewOfFile(m_hMap, FILE_MAP_READ, 0, 0, 0);
        if (!m_ptr) { CloseHandle(m_hMap); CloseHandle(m_hFile); throw std::runtime_error("View failed"); }
#else
        m_fd = open(filepath.c_str(), O_RDONLY);
        if (m_fd == -1) throw std::runtime_error("Open failed");
        
        struct stat sb;
        fstat(m_fd, &sb);
        m_size = sb.st_size;
        
        m_ptr = (char*)mmap(NULL, m_size, PROT_READ, MAP_SHARED, m_fd, 0);
        if (m_ptr == MAP_FAILED) { close(m_fd); throw std::runtime_error("mmap failed"); }
#endif
    }

    ~ReplayEngine() {
#ifdef _WIN32
        if (m_ptr) UnmapViewOfFile(m_ptr);
        if (m_hMap) CloseHandle(m_hMap);
        if (m_hFile) CloseHandle(m_hFile);
#else
        if (m_ptr) munmap(m_ptr, m_size);
        if (m_fd != -1) close(m_fd);
#endif
    }

    // Binary Search for timestamp
    size_t seek(uint64_t target_ts) {
        size_t left = 0;
        size_t right = m_size;
        size_t best_offset = m_size;

        // Simplified binary search assuming fixed record size is approximated 
        // Real implementation would handle variable payload sizes more carefully
        // or iterate if variable.
        // For canonical L2, size is constant: 64 + 968 = 1032 bytes.
        const size_t RECORD_SIZE = sizeof(EventHeader) + 968; 

        size_t num_records = m_size / RECORD_SIZE;
        size_t l = 0, r = num_records;

        while (l < r) {
            size_t mid = l + (r - l) / 2;
            size_t offset = mid * RECORD_SIZE;
            
            const EventHeader* hdr = (const EventHeader*)(m_ptr + offset);
            if (hdr->exchange_ts_ns >= target_ts) {
                best_offset = offset;
                r = mid;
            } else {
                l = mid + 1;
            }
        }
        return best_offset == m_size ? l * RECORD_SIZE : best_offset;
    }

    void play(size_t start_offset, std::function<void(const EventHeader*, const void*)> callback) {
        size_t current = start_offset;
        while (current < m_size) {
            if (current + sizeof(EventHeader) > m_size) break;
            
            const EventHeader* hdr = (const EventHeader*)(m_ptr + current);
            const void* payload = (const char*)hdr + sizeof(EventHeader);
            
            if (current + sizeof(EventHeader) + hdr->payload_size > m_size) break;

            callback(hdr, payload);
            
            current += sizeof(EventHeader) + hdr->payload_size;
        }
    }

private:
    char* m_ptr;
    size_t m_size;
#ifdef _WIN32
    HANDLE m_hFile;
    HANDLE m_hMap;
#else
    int m_fd;
#endif
};

#endif // REPLAY_ENGINE_H
