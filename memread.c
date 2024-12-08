#include <stdio.h>
#include <sys/ptrace.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/wait.h>

int main(int argc, char** argv) {
    if (argc != 5) {
        printf("Usage: %s <pid> <start_address> <end_address> <value_seeked>\n", argv[0]);
        exit(1);
    }

    // Construct the mem file path
    char mem_file_path[11 + strlen(argv[1])];
    snprintf(mem_file_path, sizeof(mem_file_path), "/proc/%s/mem", argv[1]);

    // Attach to the target process
    int pid = atoi(argv[1]);
    if (ptrace(PTRACE_ATTACH, pid, NULL, NULL) < 0) {
        perror("Unable to attach to the specified PID");
        return 1;
    }
    wait(NULL);

    // Parse start and end addresses
    long start_address = strtol(argv[2], NULL, 16);
    long end_address = strtol(argv[3], NULL, 16);

    // Allocate buffer for the memory region
    size_t buffer_size = end_address - start_address;
    char *buffer = (char *)malloc(buffer_size);
    if (!buffer) {
        perror("Failed to allocate buffer");
        ptrace(PTRACE_DETACH, pid, NULL, NULL);
        return 1;
    }

    // Open the /proc/<PID>/mem file
    int fd = open(mem_file_path, O_RDONLY);
    if (fd < 0) {
        perror("Failed to open memory file");
        free(buffer);
        ptrace(PTRACE_DETACH, pid, NULL, NULL);
        return 1;
    }

    // Read memory content into buffer
    if (pread(fd, buffer, buffer_size, start_address) != buffer_size) {
        perror("Failed to read memory");
        close(fd);
        free(buffer);
        ptrace(PTRACE_DETACH, pid, NULL, NULL);
        return 1;
    }

    close(fd);

    // Search for the value in the buffer
    int value_seeked = atoi(argv[4]);
    //int found = 0;
    for (size_t i = 0; i < buffer_size; i += sizeof(int)) {
        // Cast buffer to int and compare with the sought value
        int current_value = *(int *)(buffer + i);
        if (current_value == value_seeked) {
            printf("0x%lx\n",  start_address + i);
            //found = 1;
            //break; // Stop at the first occurrence
        }
    }

    /*if (!found) {
        printf("Value %d not found in the specified memory range.\n", value_seeked);
    }*/

    // Detach and clean up
    ptrace(PTRACE_DETACH, pid, NULL, NULL);
    free(buffer);

    return 0;
}

