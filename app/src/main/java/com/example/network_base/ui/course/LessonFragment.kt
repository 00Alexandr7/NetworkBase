package com.example.network_base.ui.course

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.network_base.NetworkBaseApplication
import com.example.network_base.R
import com.example.network_base.data.model.ContentBlock
import com.example.network_base.data.model.InfoType
import com.example.network_base.data.model.LessonWithContent
import com.example.network_base.data.model.TextStyle
import com.example.network_base.data.repository.CourseRepository
import com.example.network_base.data.repository.ProgressRepository
import com.example.network_base.databinding.FragmentLessonBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LessonFragment : Fragment() {
    
    private var _binding: FragmentLessonBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var courseRepository: CourseRepository
    private lateinit var progressRepository: ProgressRepository
    
    private var lessonId: String? = null
    private var lesson: LessonWithContent? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        lessonId = arguments?.getString("lessonId")
        
        val app = requireActivity().application as NetworkBaseApplication
        courseRepository = CourseRepository()
        progressRepository = ProgressRepository(app.database.progressDao())
        
        setupToolbar()
        setupFab()
        loadData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupFab() {
        binding.fabComplete.setOnClickListener {
            completeLesson()
        }
    }
    
    private fun loadData() {
        lessonId?.let { id ->
            lesson = courseRepository.getLessonById(id)
            lesson?.let { les ->
                binding.toolbar.title = les.title
                renderContent(les)
            }
        }
    }
    
    private fun renderContent(lesson: LessonWithContent) {
        binding.containerContent.removeAllViews()
        
        for (block in lesson.contentBlocks) {
            val view = createBlockView(block)
            view?.let {
                binding.containerContent.addView(it)
            }
        }
    }
    
    private fun createBlockView(block: ContentBlock): View {
        val context = requireContext()
        val dp16 = (16 * resources.displayMetrics.density).toInt()
        val dp8 = (8 * resources.displayMetrics.density).toInt()
        
        return when (block.type) {
            InfoType.TEXT -> {
                TextView(context).apply {
                    text = block.content
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    
                    when (block.style) {
                        TextStyle.HEADING_1 -> {
                            textSize = 24f
                            setTypeface(null, Typeface.BOLD)
                            setPadding(0, dp16, 0, dp8)
                        }
                        TextStyle.HEADING_2 -> {
                            textSize = 20f
                            setTypeface(null, Typeface.BOLD)
                            setPadding(0, dp16, 0, dp8)
                        }
                        TextStyle.HEADING_3 -> {
                            textSize = 18f
                            setTypeface(null, Typeface.BOLD)
                            setPadding(0, dp8, 0, dp8)
                        }
                        TextStyle.BOLD -> {
                            textSize = 16f
                            setTypeface(null, Typeface.BOLD)
                            setPadding(0, 0, 0, dp8)
                        }
                        TextStyle.ITALIC -> {
                            textSize = 16f
                            setTypeface(null, Typeface.ITALIC)
                            setPadding(0, 0, 0, dp8)
                        }
                        TextStyle.QUOTE -> {
                            textSize = 16f
                            setTypeface(null, Typeface.ITALIC)
                            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                            setPadding(dp16, dp8, 0, dp8)
                        }
                        TextStyle.CODE -> {
                            textSize = 14f
                            typeface = Typeface.MONOSPACE
                            setPadding(0, 0, 0, dp8)
                        }
                        TextStyle.NORMAL -> {
                            textSize = 16f
                            setPadding(0, 0, 0, dp8)
                            lineHeight = (24 * resources.displayMetrics.density).toInt()
                        }
                    }
                }
            }
            
            InfoType.CODE -> {
                MaterialCardView(context).apply {
                    radius = 8 * resources.displayMetrics.density
                    setCardBackgroundColor(ContextCompat.getColor(context, R.color.console_background))
                    
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, dp8, 0, dp8)
                    layoutParams = params
                    
                    addView(TextView(context).apply {
                        text = block.content
                        textSize = 14f
                        setTextColor(ContextCompat.getColor(context, R.color.console_text))
                        typeface = Typeface.MONOSPACE
                        setPadding(dp16, dp16, dp16, dp16)
                    })
                }
            }
            
            InfoType.NOTE, InfoType.TIP, InfoType.WARNING -> {
                MaterialCardView(context).apply {
                    val bgColor = when (block.type) {
                        InfoType.TIP -> R.color.secondary_light
                        InfoType.WARNING -> R.color.warning
                        else -> R.color.primary_light
                    }
                    setCardBackgroundColor(ContextCompat.getColor(context, bgColor))
                    radius = 8 * resources.displayMetrics.density
                    
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, dp8, 0, dp8)
                    layoutParams = params
                    
                    val textColor = when (block.type) {
                        InfoType.WARNING -> R.color.white
                        else -> R.color.text_primary
                    }
                    
                    addView(TextView(context).apply {
                        text = block.content
                        textSize = 14f
                        setTextColor(ContextCompat.getColor(context, textColor))
                        setPadding(dp16, dp16, dp16, dp16)
                    })
                }
            }
            
            InfoType.EXAMPLE, InfoType.EXERCISE -> {
                // Примеры и упражнения оформляем как отдельные карточки
                MaterialCardView(context).apply {
                    radius = 8 * resources.displayMetrics.density
                    setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_light))

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, dp8, 0, dp8)
                    layoutParams = params

                    val container = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp16, dp16, dp16, dp16)
                    }

                    if (block.content.isNotBlank()) {
                        container.addView(TextView(context).apply {
                            text = block.content
                            textSize = 16f
                            setTypeface(null, Typeface.BOLD)
                            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                        })
                    }

                    val items = block.items
                    if (items.isNotEmpty()) {
                        items.forEach { item ->
                            container.addView(TextView(context).apply {
                                text = "• $item"
                                textSize = 14f
                                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                                setPadding(0, dp8 / 2, 0, dp8 / 2)
                            })
                        }
                    } else if (block.caption?.isNotBlank() == true) {
                        container.addView(TextView(context).apply {
                            text = block.caption
                            textSize = 14f
                            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                            setPadding(0, dp8, 0, 0)
                        })
                    }

                    addView(container)
                }
            }

            InfoType.DIAGRAM, InfoType.IMAGE -> {
                // Для схем и изображений пока показываем текстовый плейсхолдер
                TextView(context).apply {
                    val caption = block.caption ?: block.resourceName
                    text = if (caption.isNullOrBlank()) block.content else "${block.content}\n$caption"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.text_hint))
                    gravity = Gravity.CENTER
                    setPadding(0, dp16, 0, dp16)
                }
            }

            else -> {
                TextView(context).apply {
                    text = block.content
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.text_hint))
                    gravity = Gravity.CENTER
                    setPadding(0, dp16, 0, dp16)
                }
            }
        }
    }
    
    private fun completeLesson() {
        lesson?.let { les ->
            viewLifecycleOwner.lifecycleScope.launch {
                progressRepository.completeLesson(les.moduleId, les.id)
                
                Snackbar.make(binding.root, "Урок завершён!", Snackbar.LENGTH_SHORT).show()
                
                findNavController().navigateUp()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
