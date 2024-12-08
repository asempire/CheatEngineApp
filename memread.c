#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/ptrace.h>
#include <sys/wait.h>
#include <errno.h>

void read_memory_range(int fd, long start_address, long end_address, int value_seeked, int pid) {
    size_t buffer_size = end_address - start_address;
    char *buffer = (char *)malloc(buffer_size);
    if (!buffer) {
        perror("Failed to allocate buffer");
        return;
    }

    // Read memory content into the buffer
    if (pread(fd, buffer, buffer_size, start_address) != buffer_size) {
        perror("Failed to read memory");
        free(buffer);
        return;
    }

    // Search for the value in the buffer
    for (size_t i = 0; i < buffer_size; i += sizeof(int)) {
        int current_value = *(int *)(buffer + i);
        if (current_value == value_seeked) {
            printf("0x%lx\n",  start_address + i);
        }
    }

    free(buffer);
}

int main(int argc, char **argv) {
    if (argc != 4) {
        printf("Usage: %s <pid> <offsets_file> <value_seeked>\n", argv[0]);
        exit(1);
    }

    // Parse the PID
    int pid = atoi(argv[1]);
    if (pid <= 0) {
        fprintf(stderr, "Invalid PID specified.\n");
        exit(1);
    }

    // Attach to the target process
    if (ptrace(PTRACE_ATTACH, pid, NULL, NULL) < 0) {
        perror("Unable to attach to the specified PID");
        return 1;
    }
    wait(NULL);

    // Open the offsets file
    FILE *offsets_file = fopen(argv[2], "r");
    if (!offsets_file) {
        perror("Failed to open offsets file");
        ptrace(PTRACE_DETACH, pid, NULL, NULL);
        return 1;
    }

    // Parse the value to search for
    int value_seeked = atoi(argv[3]);

    // Open the memory file
    char mem_file_path[64];
    snprintf(mem_file_path, sizeof(mem_file_path), "/proc/%d/mem", pid);
    int fd = open(mem_file_path, O_RDONLY);
    if (fd < 0) {
        perror("Failed to open memory file");
        fclose(offsets_file);
        ptrace(PTRACE_DETACH, pid, NULL, NULL);
        return 1;
    }

    // Read each line from the offsets file
    char line[256];
    while (fgets(line, sizeof(line), offsets_file)) {
        // Split the line into start and end addresses
        char *start_str = strtok(line, ",");
        char *end_str = strtok(NULL, ",");
        if (!start_str || !end_str) {
            fprintf(stderr, "Invalid line format: %s", line);
            continue;
        }

        // Convert addresses from string to long
        long start_address = strtol(start_str, NULL, 16);
        long end_address = strtol(end_str, NULL, 16);
        if (start_address <= 0 || end_address <= 0 || end_address <= start_address) {
            fprintf(stderr, "Invalid address range: %s", line);
            continue;
        }

        // Read and process the memory range
        read_memory_range(fd, start_address, end_address, value_seeked, pid);
    }

    // Clean up
    close(fd);
    fclose(offsets_file);
    ptrace(PTRACE_DETACH, pid, NULL, NULL);

    return 0;
}