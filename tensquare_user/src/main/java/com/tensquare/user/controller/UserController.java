package com.tensquare.user.controller;

import com.tensquare.user.pojo.User;
import com.tensquare.user.service.UserService;
import entity.PageResult;
import entity.Result;
import entity.StatusCode;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import util.JwtUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
/**
 * 控制器层
 * @author Administrator
 *
 */
@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private HttpServletRequest  request;  //获取请求request

@Autowired
private JwtUtil jwtUtil;


	/**
	 * 发送短信验证码
	 */

	@PostMapping("/sendsms/{mobile}")
	public Result sendCode(@PathVariable  String mobile){
           userService.sengSms(mobile);
           return new Result(true,StatusCode.OK,"发送成功");
	}

	/**
	 * 注册用户
	 */

	@PostMapping("/register/{code}")
	public  Result  register(@PathVariable String code,@RequestBody User user){

		userService.regist(user,code);
		return new Result(true,StatusCode.OK,"注册成功");

	}


	/**
	 * 登录用户
	 */
	@PostMapping("/login")
	public Result login(@RequestBody Map<String,String> map){
		 String mobile=map.get("mobile");
		 String pwd=map.get("password");

		 User user=userService.login(mobile,pwd);
      if(null==user){
      	return new Result(false,StatusCode.ERROR,"登录失败");
	  }

		return new Result(true,StatusCode.OK,"登陆成功");
	}



	/**
	 * 查询全部数据
	 * @return
	 */
	@RequestMapping(method= RequestMethod.GET)
	public Result findAll(){
		return new Result(true,StatusCode.OK,"查询成功",userService.findAll());
	}
	
	/**
	 * 根据ID查询
	 * @param id ID
	 * @return
	 */
	@RequestMapping(value="/{id}",method= RequestMethod.GET)
	public Result findById(@PathVariable String id){
		return new Result(true,StatusCode.OK,"查询成功",userService.findById(id));
	}


	/**
	 * 分页+多条件查询
	 * @param searchMap 查询条件封装
	 * @param page 页码
	 * @param size 页大小
	 * @return 分页结果
	 */
	@RequestMapping(value="/search/{page}/{size}",method=RequestMethod.POST)
	public Result findSearch(@RequestBody Map searchMap , @PathVariable int page, @PathVariable int size){
		Page<User> pageList = userService.findSearch(searchMap, page, size);
		return  new Result(true,StatusCode.OK,"查询成功",  new PageResult<User>(pageList.getTotalElements(), pageList.getContent()) );
	}

	/**
     * 根据条件查询
     * @param searchMap
     * @return
     */
    @RequestMapping(value="/search",method = RequestMethod.POST)
    public Result findSearch( @RequestBody Map searchMap){
        return new Result(true,StatusCode.OK,"查询成功",userService.findSearch(searchMap));
    }
	
	/**
	 * 增加
	 * @param user
	 */
	@RequestMapping(method=RequestMethod.POST)
	public Result add(@RequestBody User user  ){
		userService.add(user);
		return new Result(true,StatusCode.OK,"增加成功");
	}
	
	/**
	 * 修改
	 * @param user
	 */
	@RequestMapping(value="/{id}",method= RequestMethod.PUT)
	public Result update(@RequestBody User user, @PathVariable String id ){
		user.setId(id);
		userService.update(user);		
		return new Result(true,StatusCode.OK,"修改成功");
	}
	
	/**
	 * 删除
	 * 只有管理员才有权限删除用户
	 * @param id
	 */
	@RequestMapping(value="/{id}",method= RequestMethod.DELETE)
	public Result delete(@PathVariable String id ){

		String header = request.getHeader("Authorization");
		if(header==null){
		return new Result(false,StatusCode.ACCESSERROR,"权限不足");

	  }

		if(!header.startsWith("Bearer")) {
			return new Result(false, StatusCode.ACCESSERROR, "权限不足");
		}

	String token=header.substring(7);//提取token

	Claims claims=jwtUtil.parseJWT(token);
	if(null==claims){
		return new Result(false, StatusCode.ACCESSERROR, "权限不足");
	}
    if(!"admin".equals(claims.get("roles"))){
		return new Result(false, StatusCode.ACCESSERROR, "权限不足");
	}


		userService.deleteById(id);
		return new Result(true,StatusCode.OK,"删除成功");
	}
	
}
