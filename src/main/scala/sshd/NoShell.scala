package sshd

import org.apache.sshd.common.Factory
import org.apache.sshd.server.{ExitCallback, Environment, Command}
import java.io.{InputStream, OutputStream}
import org.eclipse.jgit.lib.Constants

/**
 * Created by IntelliJ IDEA.
 * User: denis.bardadym
 * Date: 8/4/11
 * Time: 12:27 PM
 * To change this template use File | Settings | File Templates.
 */

class NoShell extends Factory[Command] {
  def create(): Command = new Command {
    private var in: InputStream = null
    private var out: OutputStream = null
    private var err: OutputStream = null

    private var callback: ExitCallback = null

    def setInputStream(in: InputStream) {
      this.in = in
    }

    def destroy() {}

    def setExitCallback(callback: ExitCallback) {
      this.callback = callback
    }

    def start(env: Environment) {
      val message = "Test message"
      err.write(Constants.encodeASCII(message));
      in.close();
      out.close();
      err.close();
      callback.onExit(127);
    }

    def setErrorStream(err: OutputStream) {
      this.err = err
    }

    def setOutputStream(out: OutputStream) {
      this.out = out
    }
  }
}