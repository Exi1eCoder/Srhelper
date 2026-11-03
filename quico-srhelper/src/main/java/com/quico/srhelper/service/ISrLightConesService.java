package com.quico.srhelper.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.quico.srhelper.domain.SrCharacter;
import com.quico.srhelper.domain.SrLightCones;
import com.quico.srhelper.domain.dto.LightConeSaveDTO;
import com.quico.srhelper.domain.vo.CelestialLightconeVO;
import com.quico.srhelper.domain.vo.LimitedLightconeOptionVO;

/**
 * 光锥一览Service接口
 * 
 * @author quico
 * @date 2026-05-25
 */
public interface ISrLightConesService 
{
    /**
     * 查询光锥一览
     * 
     * @param id 光锥一览主键
     * @return 光锥一览
     */
    public SrLightCones selectSrLightConesById(Long id);

    /**
     * 查询光锥一览列表
     * 
     * @param srLightCones 光锥一览
     * @return 光锥一览集合
     */
    public List<SrLightCones> selectSrLightConesList(SrLightCones srLightCones);

    /**
     * 新增光锥一览
     * 
     * @param srLightCones 光锥一览
     * @return 结果
     */
    public int insertSrLightCones(SrLightCones srLightCones);

    /**
     * 修改光锥一览
     * 
     * @param srLightCones 光锥一览
     * @return 结果
     */
    public int updateSrLightCones(SrLightCones srLightCones);

    /**
     * 批量删除光锥一览
     * 
     * @param ids 需要删除的光锥一览主键集合
     * @return 结果
     */
    public int deleteSrLightConesByIds(Long[] ids);

    /**
     * 删除光锥一览信息
     * 
     * @param id 光锥一览主键
     * @return 结果
     */
    public int deleteSrLightConesById(Long id);

    /**
     * 统一保存/更新光锥及材料绑定
     */
    public Long saveAll(com.quico.srhelper.domain.dto.LightConeSaveDTO dto);

    /**
     * 获取光锥及材料绑定详情
     */
    public LightConeSaveDTO getDetail(Long id);

    
    /**
     * 批量新增光锥（统一预设 sortOrder 避免同毫秒重复）
     *
     * @param lightConesList 光锥列表
     * @return 新增的光锥ID集合
     */
    public List<Long> saveBatch(List<SrLightCones> lightConesList);

    /**
     * 导入光锥数据（按光锥名字 + 实装版本去重）
     *
     * @param list 从 Excel 解析的光锥列表
     * @param updateSupport 已存在时是否更新（false=跳过）
     * @param operName 操作人ID
     * @return 导入结果消息
     */
     public String importLightCones(List<SrLightCones> list, boolean updateSupport, String operName);

    /**
     * 批量加入星缘相遇（版本联动：按版本号取实装时间写入加入常驻池时间）
     *
     * @param ids 光锥主键集合
     * @param versionCode 版本号（必填）
     * @return 更新行数
     */
    public int addToCelestialEncounter(Long[] ids, String versionCode);

    /**
     * 查询已加入星缘相遇的光锥列表（SQL 强制 standard_type = 2）
     *
     * @param lightConeName 光锥名称（可选模糊匹配）
     * @return 星缘相遇光锥精简视图
     */
    public List<CelestialLightconeVO> selectCelestialLightconeList(String lightConeName);

    /**
     * 查询可作为星缘相遇候选的限定光锥（SQL 强制 standard_type = 0）
     *
     * @param lightConeName 光锥名称（可选模糊匹配）
     * @return 限定光锥下拉选项
     */
    public List<LimitedLightconeOptionVO> selectLimitedLightconeOptions(String lightConeName);
}
