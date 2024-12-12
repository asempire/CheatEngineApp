#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/ptrace.h>
#include <sys/wait.h>
#include <errno.h>

void write_memory(int fd, long line_address, int value_seeked, int pid) {
    
    char *buffer = (char *)malloc(sizeof(int)); //Allocate enough space or an int
    if (!buffer) {
        perror("Failed to allocate buffer");
        return;
    }
   memcpy(buffer, &value_seeked,4);
    
        // seek and read the byte at address

    if (lseek(fd, line_address, SEEK_SET) == -1) {
        perror("Failed to seek to address");
        free(buffer); ChatGPT is tryinng to give me a double free bug?
        return;
    }

    write(fd, buffer, sizeof(int));
        // Verify if value in buffer is equal to the desird value and print the address if so
    
    
    
        free(buffer);
}

int main(int argc, char **argv) {
    if (argc != 4) {
        printf("Usage: %s <pid> <address> <value_seeked>\n", argv[0]);
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

    // Parse the address to write to

    long address = strtol(argv[2], NULL, 16);
    
    // Parse the value to search for
    
    int value_seeked = atoi(argv[3]);

    // Open the memory file
    char mem_file_path[64];
    snprintf(mem_file_path, sizeof(mem_file_path), "/proc/%d/mem", pid);
    int fd = open(mem_file_path, O_WRONLY);
    if (fd < 0) {
        perror("Failed to open memory file");
        ptrace(PTRACE_DETACH, pid, NULL, NULL);
        return 1;
    }

    write_memory(fd, address, value_seeked, pid);   

    // Clean up
    close(fd);
    ptrace(PTRACE_DETACH, pid, NULL, NULL);

    return 0;
}

