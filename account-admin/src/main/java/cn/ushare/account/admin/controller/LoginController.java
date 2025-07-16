package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.util.ImageCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "LoginController", description = "登录接口")
@RestController
@Slf4j
@RequestMapping("/login")
public class LoginController {

    @Autowired
    HttpServletRequest request;
    @Autowired
    HttpServletResponse response;
    @Autowired
    SessionService sessionService;

    /**
     * 图片验证码
     */
    @ApiOperation(value="图片验证码", notes="")
    @RequestMapping(value = "/checkCode")
    public String checkCode() throws Exception {
        OutputStream os = response.getOutputStream();
        Map<String, Object> imageMap = ImageCode.getImageCode(50, 20, os);
        String checkCode = imageMap.get("strEnsure").toString().toLowerCase();
        request.getSession().setAttribute("checkCode", checkCode);
        ImageIO.write((BufferedImage) imageMap.get("image"), "JPEG", os);
        return "ok";
    }

}
