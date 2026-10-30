// compiled launcher does not open a terminal window from Steam unlike javelin.bat
// go is trivial to crosscompile
package main

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
)

func main() {
	java := filepath.Join("java", "bin", "javaw.exe")

	args := []string{
		"--enable-native-access=javelin,org.lwjgl,steamworks4j,org.lwjgl.jawt,org.lwjgl.opengl",
		"-Djava.library.path=native\\",
		"-Dorg.lwjgl.opengl.explicitInit=true",
		"-Dsun.java2d.uiScale.enabled=false",
		"-m","javelin/javelin.Javelin",
	}
	args = append(args, os.Args[1:]...)

	cmd := exec.Command(java, args...)

	// Preserve the inherited environment and extend PATH.
	cmd.Env = os.Environ()
	pathFound := false
	for i, entry := range cmd.Env {
		if strings.HasPrefix(strings.ToUpper(entry), "PATH=") {
			cmd.Env[i] = entry + ";beet\\"
			pathFound = true
			break
		}
	}
	if !pathFound {
		cmd.Env = append(cmd.Env, "PATH=beet\\")
	}

	// Print the full command before execution.
	fmt.Println("Executing command:")
	fmt.Println(quoteArg(java), quoteArgs(args))

	// Forward standard input, output, and error.
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		if exitErr, ok := err.(*exec.ExitError); ok {
			os.Exit(exitErr.ExitCode())
		}
		fmt.Fprintln(os.Stderr, "Failed to start Java:", err)
		os.Exit(1)
	}
}

// Quote an argument for readable Windows command-line output.
func quoteArg(s string) string {
	if s != "" && !strings.ContainsAny(s, " \t\n\v\"") {
		return s
	}
	return `"` + strings.ReplaceAll(s, `"`, `\"`) + `"`
}

func quoteArgs(args []string) string {
	quoted := make([]string, len(args))
	for i, arg := range args {
		quoted[i] = quoteArg(arg)
	}
	return strings.Join(quoted, " ")
}
