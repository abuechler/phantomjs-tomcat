package demo;

import java.io.IOException;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ScreenShotController {

	@Autowired
	PhantomJSDriver webDriver;

	@RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public @ResponseBody byte[] takeScreenShot(@RequestParam(defaultValue="http://www.google.com") String url) throws IOException {
		webDriver.get(url);
		return webDriver.getScreenshotAs(OutputType.BYTES);
	}
}
