#ifndef JOURNAL_WRITER_H
#define JOURNAL_WRITER_H

#include <string>
#include <fstream>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <cstring>
#include <iostream>

#ifdef _WIN32
#include <windows.h>
#else
#include <sys/mman.h>
#include <unistd.h>
#endif

#include "EventHeader.h"

class JournalWriter {
public:
    JournalWriter(const std::string& filepath, size_t capacity) 
        : m_capacity(capacity), m_offset(0) {
        
#ifdef _WIN32
        // Windows Implementation
        m_hFile = CreateFileA(filepath.c_str(), GENERIC_READ | GENERIC_WRITE, 
                             FILE_SHARE_READ, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
        
        if (m_hFile == INVALID_HANDLE_VALUE) {
            throw std::runtime_error("Failed to open file");
        }

        m_hMap = CreateFileMappingA(m_hFile, NULL, PAGE_READWRITE, 0, (DWORD)capacity, NULL);
        if (m_hMap == NULL) {
             CloseHandle(m_hFile);
             throw std::runtime_error("Failed to map file");
        }

        m_ptr = (char*)MapViewOfFile(m_hMap, FILE_MAP_ALL_ACCESS, 0, 0, capacity);
        if (m_ptr == NULL) {
            CloseHandle(m_hMap);
            CloseHandle(m_hFile);
            throw std::runtime_error("Failed to view map");
        }
#else
        // POSIX Implementation
        m_fd = open(filepath.c_str(), O_RDWR | O_CREAT, 0666);
        if (m_fd == -1) {
            throw std::runtime_error("Failed to open file");
        }
        
        if (ftruncate(m_fd, capacity) == -1) {
            close(m_fd);
            throw std::runtime_error("Failed to resize file");
        }
        
        m_ptr = (char*)mmap(NULL, capacity, PROT_READ | PROT_WRITE, MAP_SHARED, m_fd, 0);
        if (m_ptr == MAP_FAILED) {
            close(m_fd);
            throw std::runtime_error("mmap failed");
        }
#endif
    }

    ~JournalWriter() {
#ifdef _WIN32
        if (m_ptr) UnmapViewOfFile(m_ptr);
        if (m_hMap) CloseHandle(m_hMap);
        if (m_hFile) CloseHandle(m_hFile);
#else
        if (m_ptr) munmap(m_ptr, m_capacity);
        if (m_fd != -1) close(m_fd);
#endif
    }

    void append(const EventHeader& header, const void* payload) {
        size_t total_size = sizeof(EventHeader) + header.payload_size;
        
        if (m_offset + total_size > m_capacity) {
            std::cerr << "Journal full!" << std::endl;
            return;
        }
        
        // Zero-copy append (conceptually, still memcpy)
        // 1. Write Header
        std::memcpy(m_ptr + m_offset, &header, sizeof(EventHeader));
        m_offset += sizeof(EventHeader);
        
        // 2. Write Payload
        if (payload && header.payload_size > 0) {
            std::memcpy(m_ptr + m_offset, payload, header.payload_size);
            m_offset += header.payload_size;
        }
    }

private:
    size_t m_capacity;
    size_t m_offset;
    char* m_ptr;

#ifdef _WIN32
    HANDLE m_hFile;
    HANDLE m_hMap;
#else
    int m_fd;
#endif
};

#endif // JOURNAL_WRITER_H
