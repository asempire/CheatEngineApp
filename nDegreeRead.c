#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/ptrace.h>
#include <sys/wait.h>
#include <errno.h>

void read_memory(int fd, long line_address, int value_seeked, int pid) {
    
    char *buffer = (char *)malloc(sizeof(int)); //Allocate enough space or an int
    if (!buffer) {
        perror("Failed to allocate buffer");
        return;
    }

    
        // seek and read the byte at address

    if (lseek(fd, line_address, SEEK_SET) == -1) {
        perror("Failed to seek to address");
        //free(buffer); ChatGPT is tryinng to give me a double free bug?
        return;
    }

    // Use read to read the data at the specified address
    ssize_t bytes_read = read(fd, buffer, sizeof(int));
    if (bytes_read != sizeof(int)) {
        perror("Failed to read memory");
        // free(buffer); Again here!
        return;
    }

    // Verify if value in buffer is equal to the desird value and print the address if so
    
    
    
        int current_value = *(int *)(buffer);
        if (current_value == value_seeked) {
            printf("0x%lx\n",  line_address);
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
        

        // Convert addresses from string to long
        long line_address = strtol(line, NULL, 16);
        if (line_address <= 0) {
            fprintf(stderr, "Invalid address: %s", line);
            continue;
        }

        // Read and process the memory range
        read_memory(fd, line_address, value_seeked, pid);
    }

    // Clean up
    close(fd);
    fclose(offsets_file);
    ptrace(PTRACE_DETACH, pid, NULL, NULL);

    return 0;
}

